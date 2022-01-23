package com.kirtan.baseLibrary.base

interface ApiListCallingScreen<ApiRequestType : Any?, ApiResponseType, ParsedResponse> :
    ApiCallingScreen<ApiRequestType, ApiResponseType> {
    fun parseListFromResponse(response: ApiResponseType): ParsedResponse
}