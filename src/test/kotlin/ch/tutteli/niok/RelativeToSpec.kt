package ch.tutteli.niok

import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.assert
import ch.tutteli.spek.extensions.TempFolder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import java.nio.file.Paths

class RelativeToSpec : Spek({
    val tempFolder = TempFolder.perAction() //or perAction() or perGroup()
    registerListener(tempFolder)

    describe("compare Kotlin's File.relativeTo with Path.relativize") {
        val list = if (System.getProperty("os.name").startsWith("Windows")) {
            listOf(
                listOf("a", "a", "", ""),
                listOf("a", "b", "..\\a", "..\\b"),
                //Bug in JDK due to https://bugs.java.com/bugdatabase/view_bug.do?bug_id=9057443
                //listOf(".\\a", "b", "..\\a", "..$s.\\b"),
                listOf("a", ".\\b", "..\\a", "..\\.\\b")

            )
        } else {
            listOf(
                listOf("a", "a", "", ""),
                listOf("a", "b", "../a", "../b"),
                listOf("./a", "b", "../a", "../b"),
                listOf("a", "./b", "../a", "../b")

            )
        }
        list.forEach { (stringPath1, stringPath2, expectRelativeTo, expectRelativize) ->
            action("path1: $stringPath1, path2: $stringPath2 -> relativeTo: $expectRelativeTo, relativize: $expectRelativize") {
                val path1 = tempFolder.newFile(stringPath1)
                val path2 = if (stringPath1 != stringPath2) {
                    tempFolder.newFile(stringPath2)
                } else {
                    path1
                }
                val resultRelativeTo = path1.relativeTo(path2)
                test("relativeTo which delegates to Kotlin's File.relativeTo") {
                    assert(resultRelativeTo).toBe(Paths.get(expectRelativeTo))
                }
                test("path2.resolve(Path.relativeTo).normalize() = path1.normalize()") {
                    assert(path2.resolve(resultRelativeTo).normalize()).toBe(path1.normalize())
                }

                val resultRelativize = path1.relativize(path2)
                test("Path.relativize") {
                    assert(resultRelativize).toBe(Paths.get(expectRelativize))
                }
                test("path1.resolve(Path.relativize).normalize() = path2.normalize()") {
                    assert(path1.resolve(resultRelativize).normalize()).toBe(path2.normalize())
                }
            }
        }
    }
})
