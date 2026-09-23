package com.kingsley.wong.medtrack.data.api

data class OpenFdaResponse(
    val results: List<DrugLabel>? = null
)

data class DrugLabel(
    val purpose: List<String>? = null,
    val warnings: List<String>? = null,
    val dosage_and_administration: List<String>? = null,
    val active_ingredient: List<String>? = null,
    val indications_and_usage: List<String>? = null
)