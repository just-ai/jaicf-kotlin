package com.justai.jaicf.model.state

class StatePath {

    private val path: List<String>

    constructor(vararg path: String) {
        this.path = listOf(*path)
    }

    constructor(path: List<String>) {
        this.path = path
    }

    override fun toString(): String {
        return if (isRoot) {
            "/"
        } else {
            path.joinToString(separator = "/")
        }
    }

    fun stepUp(): StatePath {
        return StatePath(path.subList(0, path.size - 1))
    }

    val isRoot: Boolean
        get() = path.size == 1 && path[0] == ""

    val name: String
        get() = path[path.size - 1]

    val components: Array<String>
        get() = path.toTypedArray()

    val parents: Array<String>
        get() {
            val ret = arrayOfNulls<String>(path.size - 1)
            for (i in (path.size - 2) downTo 1) {
                ret[i] = path.subList(0, i + 1).joinToString(separator = "/")
            }
            if (path.size > 1) {
                ret[0] = "/"
            }
            return ret.requireNoNulls()
        }

    val parent: String
        get() {
            val ret = path.subList(0, path.size - 1).joinToString(separator = "/")
            return if (ret.isEmpty()) {
                "/"
            } else ret
        }

    fun resolve(subpath: String): StatePath {
        val path = parse(subpath)

        return if (subpath.startsWith("/")) {
            path.normalized()
        } else {
            StatePath(this.path + path.path).normalized()
        }
    }

    private fun normalized(): StatePath {
        val normalizedPath = mutableListOf<String>()

        for (item in this.path.withIndex()) {
            when {
                item.value == "" && item.index != 0 -> continue
                item.value == CUR -> continue
                item.value == UP -> normalizedPath.removeLastOrNull() ?: normalizedPath.add(item.value)
                else -> normalizedPath.add(item.value)
            }
        }
        return StatePath(normalizedPath)
    }

    companion object {

        private val ROOT = ""
        private val CUR = "."
        private val UP = ".."

        fun root() =
            StatePath(ROOT)

        fun parse(path: String): StatePath {
            if (path.matches(Regex("/+"))) {
                return root()
            }
            val s = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return StatePath(*s)
        }
    }
}
