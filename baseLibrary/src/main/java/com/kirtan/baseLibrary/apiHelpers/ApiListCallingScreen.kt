package com.kirtan.baseLibrary.apiHelpers

interface ApiListCallingScreen<ApiRequestType : Any?, ApiResponseType, ParsedResponse> :
    ApiCallingScreen<ApiRequestType, ApiResponseType> {
    fun parseListFromResponse(response: ApiResponseType): ParsedResponse
}