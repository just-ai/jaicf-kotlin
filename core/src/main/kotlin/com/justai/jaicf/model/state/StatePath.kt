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
        return if (path.size == 1 && path[0] == "") {
            "/"
        } else {
            path.joinToString(separator = "/")
        }
    }

    fun stepUp(): StatePath {
        return StatePath(path.subList(0, path.size - 1))
    }


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
            path
        } else {
            val s = ArrayList(this.path)
            s.addAll(path.path)
            normalize(s)
            StatePath(s)
        }
    }

    private fun normalize(items: ArrayList<String>) {
        var i = 0
        while (i < items.size) {
            val item = items[i]
            if (item == CUR) {
                items.removeAt(i--)
            } else if (i < 0) {
                break
            } else if (item == UP) {
                items.removeAt(i--)
                if (i < 0) {
                    break
                }
                items.removeAt(i--)
            }
            i++
        }
    }

    companion object {

        private val ROOT = ""
        private val CUR = "."
        private val UP = ".."

        fun root() =
            StatePath(ROOT)

        fun parse(path: String): StatePath {
            if (path == "/") {
                return root()
            }
            val s = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return StatePath(*s)
        }
    }
}