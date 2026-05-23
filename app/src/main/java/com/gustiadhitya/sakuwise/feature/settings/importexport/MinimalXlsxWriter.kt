package com.gustiadhitya.sakuwise.feature.settings.importexport

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Writes a single-sheet XLSX file with no external dependencies. */
object MinimalXlsxWriter {

    fun write(headers: List<String>, rows: List<List<String>>, out: OutputStream) {
        ZipOutputStream(out).use { zip ->
            put(zip, "[Content_Types].xml",           contentTypes())
            put(zip, "_rels/.rels",                   rootRels())
            put(zip, "xl/workbook.xml",               workbook())
            put(zip, "xl/_rels/workbook.xml.rels",    workbookRels())
            put(zip, "xl/worksheets/sheet1.xml",      sheet(headers, rows))
            put(zip, "xl/styles.xml",                 styles())
        }
    }

    private fun put(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun esc(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun colLetter(col: Int): String {
        var c = col + 1
        val sb = StringBuilder()
        while (c > 0) { val r = (c - 1) % 26; sb.insert(0, 'A' + r); c = (c - 1) / 26 }
        return sb.toString()
    }

    private fun sheet(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        sb.append("<sheetData>")

        fun appendRow(rowIdx: Int, cells: List<String>) {
            sb.append("<row r=\"${rowIdx + 1}\">")
            cells.forEachIndexed { ci, v ->
                val addr = "${colLetter(ci)}${rowIdx + 1}"
                sb.append("<c r=\"$addr\" t=\"inlineStr\"><is><t>${esc(v)}</t></is></c>")
            }
            sb.append("</row>")
        }

        appendRow(0, headers)
        rows.forEachIndexed { i, row -> appendRow(i + 1, row) }

        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun contentTypes() = """<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun rootRels() = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbook() = """<?xml version="1.0" encoding="UTF-8"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Transaksi" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

    private fun workbookRels() = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun styles() = """<?xml version="1.0" encoding="UTF-8"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
<fonts><font><sz val="11"/><name val="Calibri"/></font></fonts>
<fills><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>
<borders><border><left/><right/><top/><bottom/><diagonal/></border></borders>
<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
<cellXfs><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>"""
}
