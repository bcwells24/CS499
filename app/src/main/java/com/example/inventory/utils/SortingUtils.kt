package com.example.inventory.utils

/**
 * Utility function for sorting a mutable list using the Heap Sort algorithm.
 *
 * @param list The list to be sorted.
 * @param comparator A lambda to define the sorting order.
 */
fun <T> heapSort(list: MutableList<T>, comparator: (T, T) -> Int) {
    fun heapify(n: Int, i: Int) {
        var largest = i
        val left = 2 * i + 1
        val right = 2 * i + 2

        // Compare left child
        if (left < n && comparator(list[left], list[largest]) > 0) {
            largest = left
        }

        // Compare right child
        if (right < n && comparator(list[right], list[largest]) > 0) {
            largest = right
        }

        // If largest is not root, swap and continue heapifying
        if (largest != i) {
            list.swap(i, largest)
            heapify(n, largest)
        }
    }

    // Build the max heap
    val n = list.size
    for (i in n / 2 - 1 downTo 0) {
        heapify(n, i)
    }

    // Extract elements from the heap
    for (i in n - 1 downTo 1) {
        list.swap(0, i) // Move current root to the end
        heapify(i, 0) // Call heapify on the reduced heap
    }
}

/**
 * Extension function to swap two elements in a list.
 *
 * @param i The index of the first element.
 * @param j The index of the second element.
 */
private fun <T> MutableList<T>.swap(i: Int, j: Int) {
    val temp = this[i]
    this[i] = this[j]
    this[j] = temp
}

/* References
Gautam, Aman. (2017, December 17). Program for heapsort in kotlin. Include Help. https://www.includehelp.com/kotlin/program-for-heapsort.aspx
 */
