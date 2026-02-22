package spotilyrix.sdk.cli

import spotilyrix.sdk.search

private data class BenchRow(
    val query: String,
    val run: Int,
    val elapsedMs: Double,
    val found: Boolean,
    val length: Int,
)

fun main(args: Array<String>) {
    val parsed = parseBenchmarkArgs(args)
    if (parsed.queries.isEmpty()) {
        println("Usage: BenchmarkMainKt [--providers p1,p2] [--repeats n] <query1> <query2> ...")
        return
    }

    val rows = mutableListOf<BenchRow>()
    for (query in parsed.queries) {
        for (run in 1..parsed.repeats) {
            val start = System.nanoTime()
            val result = search(
                searchTerm = query,
                syncedOnly = true,
                providers = parsed.providers,
            )
            val elapsedMs = (System.nanoTime() - start) / 1_000_000.0
            rows += BenchRow(
                query = query,
                run = run,
                elapsedMs = elapsedMs,
                found = !result.isNullOrBlank(),
                length = result?.length ?: 0,
            )
            println("RESULT\t$query\t$run\t${"%.2f".format(elapsedMs)}\t${!result.isNullOrBlank()}\t${result?.length ?: 0}")
        }
    }

    val grouped = rows.groupBy { it.query }
    for ((query, group) in grouped) {
        val avg = group.map { it.elapsedMs }.average()
        val foundRate = group.count { it.found }.toDouble() / group.size.toDouble()
        println("SUMMARY\t$query\t${"%.2f".format(avg)}\t${"%.2f".format(foundRate)}")
    }

    val overallAvg = rows.map { it.elapsedMs }.average()
    val overallFound = rows.count { it.found }.toDouble() / rows.size.toDouble()
    println("OVERALL\t${"%.2f".format(overallAvg)}\t${"%.2f".format(overallFound)}")
}

private data class ParsedBenchmarkArgs(
    val providers: List<String>,
    val repeats: Int,
    val queries: List<String>,
)

private fun parseBenchmarkArgs(args: Array<String>): ParsedBenchmarkArgs {
    var providers = emptyList<String>()
    var repeats = 3
    val queries = mutableListOf<String>()

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--providers" -> {
                if (i + 1 < args.size) {
                    providers = args[i + 1].split(',').map { it.trim() }.filter { it.isNotBlank() }
                    i += 2
                    continue
                }
            }
            "--repeats" -> {
                if (i + 1 < args.size) {
                    repeats = args[i + 1].toIntOrNull()?.coerceAtLeast(1) ?: 3
                    i += 2
                    continue
                }
            }
            else -> queries += args[i]
        }
        i++
    }

    return ParsedBenchmarkArgs(providers, repeats, queries)
}



