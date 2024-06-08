package com.example.fitnessapp.api

import com.example.fitnessapp.domain.Exercises
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("exercises/bodyPart/{bodyPart}")
    @Headers("x-rapidapi-key: badc1e024bmshd4f64a69e866c2fp1178dcjsna09b9c38eff9")
    fun getExercises(
        @Path(PATH_BODY_PART) bodyPart: String,
        @Query(QUERY_PARAM_LIMIT) limit: Int = 10
    ): Single<Exercises>

    @GET("exercises/bodyPartList")
    @Headers("x-rapidapi-key: badc1e024bmshd4f64a69e866c2fp1178dcjsna09b9c38eff9")
    fun getBodyParts(): Single<List<String>>

    companion object {
        private const val QUERY_PARAM_LIMIT = "limit"
        private const val PATH_BODY_PART = "bodyPart"
    }
}