package com.example.dummyimageuploading

data class CloudinaryResourcesResponse(
    val resources: List<Resource>
)

data class Resource(
    val public_id: String,
    val secure_url: String
)