package com.github.arusland

import com.github.arusland.obwatch.ObsidianWatcher
import java.nio.file.Paths

fun main(args : Array<String>) {
    if (args.isEmpty()) {
        println("Usage: obwatch <targetPath> [outputPath]")
        return
    }
    val targetPath = Paths.get(args[0])
    ObsidianWatcher(targetPath).start()
}
