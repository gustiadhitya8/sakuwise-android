package com.gustiadhitya.sakuwise.feature.plan

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.Category
import com.gustiadhitya.sakuwise.core.domain.model.Plan
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.model.Recurrence
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@Serializable
data class PlanTemplate(
    val version: Int = 1,
    val templateLabel: String,
    val allocations: List<TemplateAllocation>,
)

@Serializable
data class TemplateAllocation(
    val name: String,
    val targetPct: Int,
    val sortOrder: Int,
    val categories: List<TemplateCategory>,
)

@Serializable
data class TemplateCategory(
    val name: String,
    val plannedAmount: Long,
    val sortOrder: Int,
    val items: List<TemplateItem>,
)

@Serializable
data class TemplateItem(
    val name: String,
    val plannedAmount: Long,
    val recurrence: String,
    val sortOrder: Int,
)

private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

class PlanTemplateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val planRepo: PlanRepository,
) {

    suspend fun exportTemplate(plan: Plan): Uri {
        val allocations = planRepo.observeAllocations(plan.id).first()
        val templateAllocs = allocations.map { alloc ->
            val categories = planRepo.observeCategories(alloc.id).first()
            val templateCats = categories.map { cat ->
                val items = planRepo.observePlanItems(cat.id).first()
                TemplateCategory(
                    name = cat.name,
                    plannedAmount = cat.plannedAmount ?: 0L,
                    sortOrder = cat.sortOrder,
                    items = items.map { pi ->
                        TemplateItem(
                            name = pi.name,
                            plannedAmount = pi.plannedAmount,
                            recurrence = pi.recurrence.name.lowercase(),
                            sortOrder = pi.sortOrder,
                        )
                    },
                )
            }
            TemplateAllocation(
                name = alloc.name,
                targetPct = alloc.targetPct,
                sortOrder = alloc.sortOrder,
                categories = templateCats,
            )
        }
        val template = PlanTemplate(
            templateLabel = plan.label,
            allocations = templateAllocs,
        )
        val dir = File(context.cacheDir, "templates").apply { mkdirs() }
        val slug = plan.label.replace(Regex("[^A-Za-z0-9]+"), "_").take(30)
        val file = File(dir, "sakuwise_template_$slug.json")
        file.writeText(json.encodeToString(template))
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun parseTemplate(uri: Uri): PlanTemplate {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.readText()
            ?: error(context.getString(com.gustiadhitya.sakuwise.R.string.plan_template_open_error))
        return json.decodeFromString(text)
    }

    suspend fun importTemplate(template: PlanTemplate, start: LocalDate, end: LocalDate) {
        val planId = UUID.randomUUID().toString()
        planRepo.upsert(
            Plan(
                id = planId,
                start = start,
                end = end,
                label = template.templateLabel,
                expectedIncome = 0L,
                note = null,
            ),
        )
        template.allocations.forEach { ta ->
            val allocId = UUID.randomUUID().toString()
            planRepo.upsertAllocation(
                Allocation(
                    id = allocId,
                    planId = planId,
                    name = ta.name,
                    targetPct = ta.targetPct,
                    sortOrder = ta.sortOrder,
                ),
            )
            ta.categories.forEach { tc ->
                val catId = UUID.randomUUID().toString()
                planRepo.upsertCategory(
                    Category(
                        id = catId,
                        allocationId = allocId,
                        name = tc.name,
                        plannedAmount = tc.plannedAmount,
                        sortOrder = tc.sortOrder,
                    ),
                )
                tc.items.forEach { ti ->
                    planRepo.upsertPlanItem(
                        PlanItem(
                            id = UUID.randomUUID().toString(),
                            categoryId = catId,
                            name = ti.name,
                            plannedAmount = ti.plannedAmount,
                            recurrence = runCatching {
                                Recurrence.valueOf(ti.recurrence.replaceFirstChar { it.uppercase() })
                            }.getOrDefault(Recurrence.Monthly),
                            note = null,
                            sortOrder = ti.sortOrder,
                        ),
                    )
                }
            }
        }
    }
}
