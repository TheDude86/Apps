package com.mcmlr.blocks.core

import kotlinx.coroutines.Job

open class FlowDisposer {
    companion object {
        const val DEFAULT = "default"
    }

    private val jobsMap = HashMap<String, MutableList<Job>>()

    fun addJob(collection: String, job: Job) {
        if (jobsMap.containsKey(collection)) {
            jobsMap[collection]?.add(job)
        } else {
            jobsMap[collection] = mutableListOf(job)
        }
    }

    open fun clear(collection: String) {
        jobsMap[collection]?.forEach {
            it.cancel()
        }

        jobsMap[collection] = mutableListOf()
    }

    open fun clear() {
        jobsMap.values.forEach { list ->
            list.forEach { it.cancel() }
        }

        jobsMap.clear()
    }
}