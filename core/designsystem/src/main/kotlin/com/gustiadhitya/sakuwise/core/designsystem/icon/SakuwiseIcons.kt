package com.gustiadhitya.sakuwise.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// 24×24 viewBox, 1.75 stroke, round caps/joins — ported from proto/icons.jsx
object SakuwiseIcons {

    // ── Tab bar (outline) ──────────────────────────────────────────────────

    val Home: ImageVector get() {
        if (_home != null) return _home!!
        _home = sw("Home") {
            sw {
                moveTo(4f, 11f); lineTo(12f, 4f); lineTo(20f, 11f); verticalLineTo(20f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
                horizontalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, -1f); close()
            }
            sw { moveTo(10f, 21f); verticalLineTo(14f); horizontalLineTo(14f); verticalLineTo(21f) }
        }
        return _home!!
    }
    private var _home: ImageVector? = null

    val Plan: ImageVector get() {
        if (_plan != null) return _plan!!
        _plan = sw("Plan") {
            sw { moveTo(4f, 6f); horizontalLineTo(14f) }
            sw { moveTo(4f, 12f); horizontalLineTo(14f) }
            sw { moveTo(4f, 18f); horizontalLineTo(10f) }
            sw { moveTo(17f, 5f); lineTo(19f, 7f); lineTo(22f, 4f) }
            sw { moveTo(17f, 11f); lineTo(19f, 13f); lineTo(22f, 10f) }
        }
        return _plan!!
    }
    private var _plan: ImageVector? = null

    val Plus: ImageVector get() {
        if (_plus != null) return _plus!!
        _plus = sw("Plus") {
            sw { moveTo(12f, 5f); verticalLineTo(19f) }
            sw { moveTo(5f, 12f); horizontalLineTo(19f) }
        }
        return _plus!!
    }
    private var _plus: ImageVector? = null

    val Assets: ImageVector get() {
        if (_assets != null) return _assets!!
        _assets = sw("Assets") {
            sw {
                moveTo(5f, 6f); arcTo(7f, 2.5f, 0f, true, false, 19f, 6f)
                arcTo(7f, 2.5f, 0f, true, false, 5f, 6f); close()
            }
            sw {
                moveTo(5f, 6f); verticalLineTo(11f)
                curveToRelative(0f, 1.4f, 3.1f, 2.5f, 7f, 2.5f)
                reflectiveCurveToRelative(7f, -1.1f, 7f, -2.5f)
                verticalLineTo(6f)
            }
            sw {
                moveTo(5f, 11f); verticalLineTo(16f)
                curveToRelative(0f, 1.4f, 3.1f, 2.5f, 7f, 2.5f)
                reflectiveCurveToRelative(7f, -1.1f, 7f, -2.5f)
                verticalLineTo(11f)
            }
            sw {
                moveTo(5f, 16f); verticalLineTo(19f)
                curveToRelative(0f, 1.4f, 3.1f, 2.5f, 7f, 2.5f)
                reflectiveCurveToRelative(7f, -1.1f, 7f, -2.5f)
                verticalLineTo(16f)
            }
        }
        return _assets!!
    }
    private var _assets: ImageVector? = null

    val Me: ImageVector get() {
        if (_me != null) return _me!!
        _me = sw("Me") {
            sw { circle(12f, 8.5f, 3.5f) }
            sw {
                moveTo(5f, 20f)
                curveToRelative(1.5f, -3.5f, 4.2f, -5f, 7f, -5f)
                reflectiveCurveToRelative(5.5f, 1.5f, 7f, 5f)
            }
        }
        return _me!!
    }
    private var _me: ImageVector? = null

    // ── Tab bar (filled variants) ──────────────────────────────────────────

    val HomeFilled: ImageVector get() {
        if (_homeFilled != null) return _homeFilled!!
        _homeFilled = sw("HomeFilled") {
            fill {
                moveTo(4f, 11f); lineTo(12f, 4f); lineTo(20f, 11f); verticalLineTo(20f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
                horizontalLineTo(14f); verticalLineTo(14f); horizontalLineTo(10f); verticalLineTo(21f)
                horizontalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, -1f); close()
            }
        }
        return _homeFilled!!
    }
    private var _homeFilled: ImageVector? = null

    val PlanFilled: ImageVector get() {
        if (_planFilled != null) return _planFilled!!
        _planFilled = sw("PlanFilled") {
            fill { moveTo(3f, 5f); horizontalLineTo(15f); verticalLineTo(7f); horizontalLineTo(3f); close() }
            fill { moveTo(3f, 11f); horizontalLineTo(15f); verticalLineTo(13f); horizontalLineTo(3f); close() }
            fill { moveTo(3f, 17f); horizontalLineTo(11f); verticalLineTo(19f); horizontalLineTo(3f); close() }
            fill {
                moveTo(17f, 5f); lineTo(19f, 7f); lineTo(22f, 4f)
                lineTo(23.4f, 5.4f); lineTo(19f, 9.8f); lineTo(15.6f, 6.4f); close()
            }
        }
        return _planFilled!!
    }
    private var _planFilled: ImageVector? = null

    val AssetsFilled: ImageVector get() {
        if (_assetsFilled != null) return _assetsFilled!!
        _assetsFilled = sw("AssetsFilled") {
            fill {
                moveTo(5f, 6f); arcTo(7f, 2.5f, 0f, true, false, 19f, 6f)
                arcTo(7f, 2.5f, 0f, true, false, 5f, 6f); close()
            }
            fill {
                moveTo(5f, 6f); verticalLineTo(11f)
                curveToRelative(0f, 1.4f, 3.1f, 2.5f, 7f, 2.5f)
                reflectiveCurveToRelative(7f, -1.1f, 7f, -2.5f)
                verticalLineTo(6f); close()
            }
            fill {
                moveTo(5f, 11f); verticalLineTo(16f)
                curveToRelative(0f, 1.4f, 3.1f, 2.5f, 7f, 2.5f)
                reflectiveCurveToRelative(7f, -1.1f, 7f, -2.5f)
                verticalLineTo(11f); close()
            }
            fill {
                moveTo(5f, 16f); verticalLineTo(19f)
                curveToRelative(0f, 1.4f, 3.1f, 2.5f, 7f, 2.5f)
                reflectiveCurveToRelative(7f, -1.1f, 7f, -2.5f)
                verticalLineTo(16f); close()
            }
        }
        return _assetsFilled!!
    }
    private var _assetsFilled: ImageVector? = null

    val MeFilled: ImageVector get() {
        if (_meFilled != null) return _meFilled!!
        _meFilled = sw("MeFilled") {
            fill { circle(12f, 8f, 4f) }
            fill {
                moveTo(4f, 21f)
                curveToRelative(1.5f, -4.5f, 4.5f, -6.5f, 8f, -6.5f)
                reflectiveCurveToRelative(6.5f, 2f, 8f, 6.5f); close()
            }
        }
        return _meFilled!!
    }
    private var _meFilled: ImageVector? = null

    // ── Navigation / chrome ────────────────────────────────────────────────

    val Back: ImageVector get() {
        if (_back != null) return _back!!
        _back = sw("Back") { sw { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) } }
        return _back!!
    }
    private var _back: ImageVector? = null

    val Close: ImageVector get() {
        if (_close != null) return _close!!
        _close = sw("Close") {
            sw { moveTo(6f, 6f); lineTo(18f, 18f) }
            sw { moveTo(18f, 6f); lineTo(6f, 18f) }
        }
        return _close!!
    }
    private var _close: ImageVector? = null

    val More: ImageVector get() {
        if (_more != null) return _more!!
        _more = sw("More") {
            sw {
                circle(5f, 12f, 1f)
                circle(12f, 12f, 1f)
                circle(19f, 12f, 1f)
            }
        }
        return _more!!
    }
    private var _more: ImageVector? = null

    val Search: ImageVector get() {
        if (_search != null) return _search!!
        _search = sw("Search") {
            sw { circle(11f, 11f, 6f) }
            sw { moveTo(16f, 16f); lineTo(21f, 21f) }
        }
        return _search!!
    }
    private var _search: ImageVector? = null

    val ChevronRight: ImageVector get() {
        if (_chevronRight != null) return _chevronRight!!
        _chevronRight = sw("ChevronRight") { sw { moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f) } }
        return _chevronRight!!
    }
    private var _chevronRight: ImageVector? = null

    val ChevronDown: ImageVector get() {
        if (_chevronDown != null) return _chevronDown!!
        _chevronDown = sw("ChevronDown") { sw { moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f) } }
        return _chevronDown!!
    }
    private var _chevronDown: ImageVector? = null

    val ChevronUp: ImageVector get() {
        if (_chevronUp != null) return _chevronUp!!
        _chevronUp = sw("ChevronUp") { sw { moveTo(6f, 15f); lineTo(12f, 9f); lineTo(18f, 15f) } }
        return _chevronUp!!
    }
    private var _chevronUp: ImageVector? = null

    // ── Actions ────────────────────────────────────────────────────────────

    val Edit: ImageVector get() {
        if (_edit != null) return _edit!!
        _edit = sw("Edit") {
            sw { moveTo(4f, 20f); lineTo(8f, 19f); lineTo(19f, 8f); lineTo(16f, 5f); lineTo(5f, 16f); close() }
            sw { moveTo(14f, 7f); lineTo(17f, 10f) }
        }
        return _edit!!
    }
    private var _edit: ImageVector? = null

    val Trash: ImageVector get() {
        if (_trash != null) return _trash!!
        _trash = sw("Trash") {
            sw { moveTo(5f, 7f); horizontalLineTo(19f) }
            sw {
                moveTo(9f, 7f); verticalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                horizontalLineTo(14f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
                verticalLineTo(7f)
            }
            sw {
                moveTo(7f, 7f); lineTo(8f, 20f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, 1f)
                horizontalLineTo(15f)
                arcToRelative(1f, 1f, 0f, false, false, 1f, -1f)
                lineTo(17f, 7f)
            }
        }
        return _trash!!
    }
    private var _trash: ImageVector? = null

    val Copy: ImageVector get() {
        if (_copy != null) return _copy!!
        _copy = sw("Copy") {
            sw { roundedRect(8f, 8f, 12f, 12f, 2f) }
            sw {
                moveTo(16f, 8f); verticalLineTo(6f)
                arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
                horizontalLineTo(6f)
                arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
                verticalLineTo(14f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineTo(8f)
            }
        }
        return _copy!!
    }
    private var _copy: ImageVector? = null

    val Check: ImageVector get() {
        if (_check != null) return _check!!
        _check = sw("Check") { sw { moveTo(5f, 12f); lineTo(10f, 17f); lineTo(19f, 7f) } }
        return _check!!
    }
    private var _check: ImageVector? = null

    val Camera: ImageVector get() {
        if (_camera != null) return _camera!!
        _camera = sw("Camera") {
            sw {
                moveTo(4f, 8f); horizontalLineTo(7f); lineTo(9f, 5f)
                horizontalLineTo(15f); lineTo(17f, 8f); horizontalLineTo(20f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
                verticalLineTo(18f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, 1f)
                horizontalLineTo(4f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, -1f)
                verticalLineTo(9f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, -1f)
                close()
            }
            sw { circle(12f, 13f, 3.5f) }
        }
        return _camera!!
    }
    private var _camera: ImageVector? = null

    val Calendar: ImageVector get() {
        if (_calendar != null) return _calendar!!
        _calendar = sw("Calendar") {
            sw { roundedRect(3f, 5f, 18f, 16f, 2f) }
            sw { moveTo(3f, 10f); horizontalLineTo(21f) }
            sw { moveTo(8f, 3f); verticalLineTo(7f) }
            sw { moveTo(16f, 3f); verticalLineTo(7f) }
        }
        return _calendar!!
    }
    private var _calendar: ImageVector? = null

    val Filter: ImageVector get() {
        if (_filter != null) return _filter!!
        _filter = sw("Filter") {
            sw { moveTo(4f, 5f); horizontalLineTo(20f) }
            sw { moveTo(7f, 12f); horizontalLineTo(17f) }
            sw { moveTo(10f, 19f); horizontalLineTo(14f) }
        }
        return _filter!!
    }
    private var _filter: ImageVector? = null

    val Bell: ImageVector get() {
        if (_bell != null) return _bell!!
        _bell = sw("Bell") {
            sw {
                moveTo(6f, 16f); verticalLineTo(11f)
                arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
                verticalLineTo(16f); lineTo(20f, 18f); horizontalLineTo(4f); close()
            }
            sw { moveTo(10f, 21f); arcToRelative(2f, 2f, 0f, false, false, 4f, 0f) }
        }
        return _bell!!
    }
    private var _bell: ImageVector? = null

    val Shield: ImageVector get() {
        if (_shield != null) return _shield!!
        _shield = sw("Shield") {
            sw {
                moveTo(12f, 3f); lineTo(20f, 6f); verticalLineTo(12f)
                curveToRelative(0f, 4f, -3f, 7.5f, -8f, 9f)
                curveToRelative(-5f, -1.5f, -8f, -5f, -8f, -9f)
                verticalLineTo(6f); close()
            }
        }
        return _shield!!
    }
    private var _shield: ImageVector? = null

    val Eye: ImageVector get() {
        if (_eye != null) return _eye!!
        _eye = sw("Eye") {
            sw {
                moveTo(2f, 12f)
                curveTo(5f, 6f, 9f, 4f, 12f, 4f)
                reflectiveCurveToRelative(7f, 2f, 10f, 8f)
                curveToRelative(-3f, 6f, -7f, 8f, -10f, 8f)
                reflectiveCurveToRelative(-7f, -2f, -10f, -8f)
                close()
            }
            sw { circle(12f, 12f, 3f) }
        }
        return _eye!!
    }
    private var _eye: ImageVector? = null

    val EyeOff: ImageVector get() {
        if (_eyeOff != null) return _eyeOff!!
        _eyeOff = sw("EyeOff") {
            sw { moveTo(3f, 12f); curveToRelative(1.6f, -3.2f, 3.7f, -5.3f, 6f, -6.5f) }
            sw {
                moveTo(9.7f, 5f)
                arcToRelative(8f, 8f, 0f, false, true, 2.3f, -0.3f)
                curveToRelative(4f, 0f, 7f, 2.5f, 9f, 7f)
                curveToRelative(-0.7f, 1.6f, -1.6f, 2.9f, -2.6f, 4f)
            }
            sw { moveTo(14.5f, 14f); arcToRelative(3f, 3f, 0f, false, true, -4.2f, -4.2f) }
            sw { moveTo(4f, 4f); lineTo(20f, 20f) }
        }
        return _eyeOff!!
    }
    private var _eyeOff: ImageVector? = null

    // ── Transaction types ──────────────────────────────────────────────────

    val Expense: ImageVector get() {
        if (_expense != null) return _expense!!
        _expense = sw("Expense") {
            sw { circle(12f, 12f, 9f) }
            sw { moveTo(12f, 7f); verticalLineTo(17f) }
            sw { moveTo(8f, 13f); lineTo(12f, 17f); lineTo(16f, 13f) }
        }
        return _expense!!
    }
    private var _expense: ImageVector? = null

    val Income: ImageVector get() {
        if (_income != null) return _income!!
        _income = sw("Income") {
            sw { circle(12f, 12f, 9f) }
            sw { moveTo(12f, 17f); verticalLineTo(7f) }
            sw { moveTo(8f, 11f); lineTo(12f, 7f); lineTo(16f, 11f) }
        }
        return _income!!
    }
    private var _income: ImageVector? = null

    val Transfer: ImageVector get() {
        if (_transfer != null) return _transfer!!
        _transfer = sw("Transfer") {
            sw { circle(12f, 12f, 9f) }
            sw { moveTo(7f, 10f); horizontalLineTo(17f); lineTo(14f, 7f) }
            sw { moveTo(17f, 14f); horizontalLineTo(7f); lineTo(10f, 17f) }
        }
        return _transfer!!
    }
    private var _transfer: ImageVector? = null

    // ── Account types ──────────────────────────────────────────────────────

    val Cash: ImageVector get() {
        if (_cash != null) return _cash!!
        _cash = sw("Cash") {
            sw { roundedRect(3f, 7f, 18f, 11f, 2f) }
            sw { circle(12f, 12.5f, 2.5f) }
        }
        return _cash!!
    }
    private var _cash: ImageVector? = null

    val Bank: ImageVector get() {
        if (_bank != null) return _bank!!
        _bank = sw("Bank") {
            sw { moveTo(3f, 21f); horizontalLineTo(21f) }
            sw { moveTo(5f, 10f); horizontalLineTo(19f); verticalLineTo(18f); horizontalLineTo(5f); close() }
            sw { moveTo(5f, 10f); lineTo(12f, 4f); lineTo(19f, 10f) }
            sw {
                moveTo(9f, 13f); verticalLineTo(16f)
                moveTo(12f, 13f); verticalLineTo(16f)
                moveTo(15f, 13f); verticalLineTo(16f)
            }
        }
        return _bank!!
    }
    private var _bank: ImageVector? = null

    val Wallet: ImageVector get() {
        if (_wallet != null) return _wallet!!
        _wallet = sw("Wallet") {
            sw {
                moveTo(4f, 7f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                horizontalLineTo(17f)
                arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
                verticalLineTo(8f)
            }
            sw {
                moveTo(4f, 7f); verticalLineTo(18f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                horizontalLineTo(18f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                verticalLineTo(11f)
                arcToRelative(1f, 1f, 0f, false, false, -1f, -1f)
                horizontalLineTo(5f)
                arcToRelative(1f, 1f, 0f, false, true, -1f, -1f)
                verticalLineTo(7f)
            }
            // Filled dot (coin indicator)
            fill { circle(16f, 14.5f, 1.3f) }
        }
        return _wallet!!
    }
    private var _wallet: ImageVector? = null

    // ── Asset types ────────────────────────────────────────────────────────

    val Gold: ImageVector get() {
        if (_gold != null) return _gold!!
        _gold = sw("Gold") {
            sw { moveTo(6f, 6f); horizontalLineTo(18f); lineTo(20f, 11f); lineTo(12f, 21f); lineTo(4f, 11f); close() }
            sw { moveTo(6f, 6f); lineTo(8f, 11f); horizontalLineTo(16f); lineTo(18f, 6f) }
            sw { moveTo(4f, 11f); horizontalLineTo(20f) }
            sw { moveTo(12f, 11f); verticalLineTo(21f) }
        }
        return _gold!!
    }
    private var _gold: ImageVector? = null

    val Land: ImageVector get() {
        if (_land != null) return _land!!
        _land = sw("Land") {
            sw { moveTo(3f, 19f); horizontalLineTo(21f) }
            sw { moveTo(5f, 19f); verticalLineTo(13f); lineTo(9f, 10f); lineTo(13f, 13f); verticalLineTo(19f) }
            sw { moveTo(13f, 19f); verticalLineTo(11f); lineTo(18f, 8f); lineTo(21f, 11f); verticalLineTo(19f) }
            sw {
                moveTo(9f, 19f); verticalLineTo(15f)
                moveTo(16f, 19f); verticalLineTo(13f)
            }
        }
        return _land!!
    }
    private var _land: ImageVector? = null

    val Deposit: ImageVector get() {
        if (_deposit != null) return _deposit!!
        _deposit = sw("Deposit") {
            sw { circle(12f, 12f, 9f) }
            sw {
                moveTo(9f, 9f); horizontalLineTo(13.5f)
                arcToRelative(2f, 2f, 0f, true, true, 0f, 4f)
                horizontalLineTo(10f)
                arcToRelative(2f, 2f, 0f, true, false, 0f, 4f)
                horizontalLineTo(15f)
            }
        }
        return _deposit!!
    }
    private var _deposit: ImageVector? = null

    // ── Misc ───────────────────────────────────────────────────────────────

    val Link: ImageVector get() {
        if (_link != null) return _link!!
        _link = sw("Link") {
            sw { moveTo(10f, 14f); lineTo(14f, 10f) }
            sw {
                moveTo(7f, 13f); lineTo(4f, 16f)
                arcToRelative(3f, 3f, 0f, false, false, 4.2f, 4.2f)
                lineTo(11f, 17.5f)
            }
            sw {
                moveTo(13f, 6.5f); lineTo(15.8f, 3.8f)
                arcToRelative(3f, 3f, 0f, false, true, 4.2f, 4.2f)
                lineTo(17f, 11f)
            }
        }
        return _link!!
    }
    private var _link: ImageVector? = null

    val Receipt: ImageVector get() {
        if (_receipt != null) return _receipt!!
        _receipt = sw("Receipt") {
            sw {
                moveTo(6f, 3f)
                lineTo(7f, 4f); lineTo(8f, 3f); lineTo(9f, 4f); lineTo(10f, 3f)
                lineTo(11f, 4f); lineTo(12f, 3f); lineTo(13f, 4f); lineTo(14f, 3f)
                lineTo(15f, 4f); lineTo(16f, 3f); lineTo(17f, 4f); lineTo(18f, 3f)
                verticalLineTo(21f)
                lineTo(17f, 20f); lineTo(16f, 21f); lineTo(15f, 20f); lineTo(14f, 21f)
                lineTo(13f, 20f); lineTo(12f, 21f); lineTo(11f, 20f); lineTo(10f, 21f)
                lineTo(9f, 20f); lineTo(8f, 21f); lineTo(7f, 20f); lineTo(6f, 21f)
                close()
            }
            sw { moveTo(9f, 9f); horizontalLineTo(15f) }
            sw { moveTo(9f, 13f); horizontalLineTo(15f) }
        }
        return _receipt!!
    }
    private var _receipt: ImageVector? = null

    val ArrowUpRight: ImageVector get() {
        if (_arrowUpRight != null) return _arrowUpRight!!
        _arrowUpRight = sw("ArrowUpRight") {
            sw { moveTo(7f, 17f); lineTo(17f, 7f) }
            sw { moveTo(8f, 7f); horizontalLineTo(17f); verticalLineTo(16f) }
        }
        return _arrowUpRight!!
    }
    private var _arrowUpRight: ImageVector? = null

    val ArrowDownLeft: ImageVector get() {
        if (_arrowDownLeft != null) return _arrowDownLeft!!
        _arrowDownLeft = sw("ArrowDownLeft") {
            sw { moveTo(17f, 7f); lineTo(7f, 17f) }
            sw { moveTo(16f, 17f); horizontalLineTo(7f); verticalLineTo(8f) }
        }
        return _arrowDownLeft!!
    }
    private var _arrowDownLeft: ImageVector? = null

    val Swap: ImageVector get() {
        if (_swap != null) return _swap!!
        _swap = sw("Swap") {
            sw { moveTo(5f, 8f); horizontalLineTo(17f); lineTo(14f, 5f) }
            sw { moveTo(19f, 16f); horizontalLineTo(7f); lineTo(10f, 19f) }
        }
        return _swap!!
    }
    private var _swap: ImageVector? = null

    val Leaf: ImageVector get() {
        if (_leaf != null) return _leaf!!
        _leaf = sw("Leaf") {
            sw {
                moveTo(12f, 4f)
                curveTo(19f, 7f, 19f, 17f, 12f, 20f)
                curveTo(5f, 17f, 5f, 7f, 12f, 4f)
                close()
            }
            sw { moveTo(12f, 6f); verticalLineTo(18f) }
        }
        return _leaf!!
    }
    private var _leaf: ImageVector? = null

    val Warning: ImageVector get() {
        if (_warning != null) return _warning!!
        _warning = sw("Warning") {
            sw { moveTo(12f, 3f); lineTo(22f, 20f); horizontalLineTo(2f); close() }
            sw { moveTo(12f, 10f); verticalLineTo(14f) }
            fill { circle(12f, 17f, 0.6f) }
        }
        return _warning!!
    }
    private var _warning: ImageVector? = null

    val Info: ImageVector get() {
        if (_info != null) return _info!!
        _info = sw("Info") {
            sw { circle(12f, 12f, 9f) }
            sw { moveTo(12f, 11f); verticalLineTo(16f) }
            fill { circle(12f, 8f, 0.6f) }
        }
        return _info!!
    }
    private var _info: ImageVector? = null

    val Sparkle: ImageVector get() {
        if (_sparkle != null) return _sparkle!!
        _sparkle = sw("Sparkle") {
            sw {
                moveTo(12f, 4f); lineTo(13.5f, 10f); lineTo(19f, 11.5f)
                lineTo(13.5f, 13f); lineTo(12f, 19f); lineTo(10.5f, 13f)
                lineTo(5f, 11.5f); lineTo(10.5f, 10f); close()
            }
        }
        return _sparkle!!
    }
    private var _sparkle: ImageVector? = null
}

// ── Builder helpers ────────────────────────────────────────────────────────

private fun sw(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = "sw.$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(block).build()

private fun ImageVector.Builder.sw(block: PathBuilder.() -> Unit) = path(
    fill = SolidColor(Color.Transparent),
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 1.75f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = block,
)

private fun ImageVector.Builder.fill(block: PathBuilder.() -> Unit) = path(
    fill = SolidColor(Color.Black),
    pathFillType = PathFillType.NonZero,
    pathBuilder = block,
)

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, true, false, cx + r, cy)
    arcTo(r, r, 0f, true, false, cx - r, cy)
    close()
}

private fun PathBuilder.roundedRect(x: Float, y: Float, w: Float, h: Float, rx: Float) {
    moveTo(x + rx, y)
    horizontalLineTo(x + w - rx)
    arcToRelative(rx, rx, 0f, false, true, rx, rx)
    verticalLineTo(y + h - rx)
    arcToRelative(rx, rx, 0f, false, true, -rx, rx)
    horizontalLineTo(x + rx)
    arcToRelative(rx, rx, 0f, false, true, -rx, -rx)
    verticalLineTo(y + rx)
    arcToRelative(rx, rx, 0f, false, true, rx, -rx)
    close()
}
