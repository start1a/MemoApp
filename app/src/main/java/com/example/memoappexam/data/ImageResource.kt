package com.example.memoappexam.data

// ListAdapter tag로 넘겨 줄 이미지 정보
data class ImageResource(
    // intent로 넘길 이미지 파일 경로
    var imagePath: String,
    // 삭제 모드일 경우 해당 이미지 인덱스 번호
    var selectedIndex: Int
)