import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("sakuwise.android.library")
        pluginManager.apply("sakuwise.android.library.compose")
        pluginManager.apply("sakuwise.hilt")
    }
}
