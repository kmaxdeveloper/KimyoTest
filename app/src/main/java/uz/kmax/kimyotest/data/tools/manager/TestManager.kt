package uz.kmax.kimyotest.data.tools.manager

import uz.kmax.kimyotest.domain.models.main.BaseTestData


class TestManager() {
    var currentQuestionPosition: Int = 0
    var correctAnswerCount = 0
    var wrongAnswerCount = 0
    var percent = 0
    private var testQuestionsList = ArrayList<BaseTestData>()

    fun setTestList(testList : ArrayList<BaseTestData>){
        testQuestionsList.clear()
        testQuestionsList.addAll(testList)
    }

    fun getQuestion(): String {
        if (testQuestionsList.isEmpty() || currentQuestionPosition !in testQuestionsList.indices) return ""
        return testQuestionsList[currentQuestionPosition].question
    }

    fun getAnswer(): String {
        if (testQuestionsList.isEmpty() || currentQuestionPosition !in testQuestionsList.indices) return ""
        return testQuestionsList[currentQuestionPosition].answer
    }

    fun getVariantA(): String {
        if (testQuestionsList.isEmpty() || currentQuestionPosition !in testQuestionsList.indices) return ""
        return testQuestionsList[currentQuestionPosition].variantA
    }

    fun getPercent(): Double {
        val size = getQuestionSize()
        if (size == 0) return 0.0
        return ((100.0 / size) * correctAnswerCount)
    }

    fun getVariantB(): String {
        if (testQuestionsList.isEmpty() || currentQuestionPosition !in testQuestionsList.indices) return ""
        return testQuestionsList[currentQuestionPosition].variantB
    }

    fun getVariantC(): String {
        if (testQuestionsList.isEmpty() || currentQuestionPosition !in testQuestionsList.indices) return ""
        return testQuestionsList[currentQuestionPosition].variantC
    }

    fun getVariantD(): String {
        if (testQuestionsList.isEmpty() || currentQuestionPosition !in testQuestionsList.indices) return ""
        return testQuestionsList[currentQuestionPosition].variantD
    }

    fun getQuestionSize() = testQuestionsList.size

    fun hasNextQuestion(): Boolean {
        if (currentQuestionPosition < getQuestionSize() - 1) {
            currentQuestionPosition++
            return true
        }
        return false
    }

    fun checkAnswer(answer: String) {
        if (answer == getAnswer()) {
            correctAnswerCount++
        } else {
            wrongAnswerCount++
        }
    }

    fun checkAnswerBoolean(answer: String) : Boolean{
        if (answer == getAnswer()) {
            return true
        }
        return false
    }

    fun answerWithPercent(): Int {
        val size = getQuestionSize()
        if (size == 0) return 0
        return correctAnswerCount * 100 / size
    }

}