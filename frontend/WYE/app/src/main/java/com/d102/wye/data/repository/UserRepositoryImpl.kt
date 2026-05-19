package com.d102.wye.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import com.d102.wye.data.mapper.toDomain
import com.d102.wye.data.remote.api.UserApiService
import com.d102.wye.data.remote.dto.request.UpdateUserProfileRequest
import com.d102.wye.data.remote.dto.response.BaseResponse
import com.d102.wye.data.remote.dto.response.FavoriteEtfListResponse
import com.d102.wye.domain.common.ApiError
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.common.map
import com.d102.wye.domain.model.FavoriteEtfList
import com.d102.wye.domain.model.FavoriteEtfSort
import com.d102.wye.domain.model.MyDataHolding
import com.d102.wye.domain.model.UserProfile
import com.d102.wye.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    @ApplicationContext private val context: Context
) : BaseRepository(), UserRepository {

    private val _favoriteEtfChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val favoriteEtfChanged: Flow<Unit> = _favoriteEtfChanged.asSharedFlow()

    /** 마이페이지에서 사용할 내 프로필 정보를 서버에서 조회한다. */
    override suspend fun getMyProfile(): BaseResult<UserProfile> {
        return safeApiCall {
            userApiService.getMyProfile()
        }.map { it.toDomain() }
    }

    override suspend fun getFavoriteEtfs(sort: FavoriteEtfSort): BaseResult<FavoriteEtfList> {
        var rawBody: String? = null
        return try {
            Timber.d("[FavoriteEtf] list request | sort=${sort.queryValue}")
            val response = userApiService.getFavoriteEtfsRaw(sort = sort.queryValue)
            rawBody = if (response.isSuccessful) {
                response.body()?.string()
            } else {
                response.errorBody()?.string()
            }

            Timber.d("[FavoriteEtf] list raw response | body=$rawBody")

            if (!response.isSuccessful) {
                BaseResult.Error(
                    ApiError.unknownError(
                        "관심 ETF 목록 조회 실패 | http=${response.code()} | body=$rawBody"
                    )
                )
            } else if (rawBody.isNullOrBlank()) {
                BaseResult.Error(ApiError.unknownError("관심 ETF 목록 응답 데이터가 없습니다."))
            } else {
                val responseType = object : TypeToken<BaseResponse<FavoriteEtfListResponse>>() {}.type
                val parsedBody: BaseResponse<FavoriteEtfListResponse> = Gson().fromJson(rawBody, responseType)
                val data = parsedBody.data

                if (data == null) {
                    BaseResult.Error(
                        ApiError.unknownError(
                            parsedBody.message ?: "관심 ETF 목록 응답 data가 없습니다."
                        )
                    )
                } else {
                    Timber.d(
                        "[FavoriteEtf] list parsed | totalCount=${data.totalCount} | first=${data.favorites.firstOrNull()}"
                    )
                    data.favorites.forEach { favorite ->
                        Timber.d(
                            "[FavoriteEtf] item | ticker=${favorite.ticker} | name=${favorite.name} | riskType=${favorite.riskType} | assetManager=${favorite.assetManager} | currentPrice=${favorite.currentPrice} | changeRate=${favorite.changeRate} | favoritedAt=${favorite.favoritedAt}"
                        )
                    }
                    BaseResult.Success(data.toDomain())
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "[FavoriteEtf] list parse failed | rawBody=$rawBody")
            BaseResult.Error(ApiError.unknownError(e.message ?: "관심 ETF 목록 파싱 중 오류가 발생했습니다."))
        }
    }

    override suspend fun checkFavoriteEtf(ticker: String): BaseResult<Boolean> {
        return safeApiCall {
            userApiService.checkFavoriteEtf(ticker = ticker)
        }
    }

    override suspend fun addFavoriteEtf(ticker: String): BaseResult<Unit> {
        Timber.d("[FavoriteEtf] add request | ticker=$ticker")
        return safeApiCallWithoutData(
            onSuccess = { _favoriteEtfChanged.tryEmit(Unit) }
        ) {
            userApiService.addFavoriteEtf(ticker = ticker)
        }
    }

    override suspend fun deleteFavoriteEtf(ticker: String): BaseResult<Unit> {
        Timber.d("[FavoriteEtf] delete request | ticker=$ticker")
        return safeApiCallWithoutData(
            onSuccess = { _favoriteEtfChanged.tryEmit(Unit) }
        ) {
            userApiService.deleteFavoriteEtf(ticker = ticker)
        }
    }

    /** PATCH users/me로 닉네임/프로필 이미지를 수정한 뒤 최신 프로필을 반환한다. */
    override suspend fun updateMyProfile(
        nickname: String?,
        profileImage: String?
    ): BaseResult<UserProfile> {
        val updateResult = safeApiCall {
            userApiService.updateMyProfile(
                UpdateUserProfileRequest(
                    nickname = nickname,
                    profileImage = profileImage
                )
            )
        }

        return when (updateResult) {
            is BaseResult.Success -> getMyProfile()
            is BaseResult.Error -> updateResult
        }
    }

    override suspend fun uploadProfileImage(imageUri: String): BaseResult<UserProfile> {
        Timber.d("[ProfileImage] upload requested | uri=$imageUri")

        when (val validation = validateProfileImage(imageUri)) {
            is BaseResult.Error -> {
                Timber.w("[ProfileImage] validation failed | message=${validation.error.message}")
                return validation
            }
            is BaseResult.Success -> Unit
        }

        val filePart = buildProfileImagePart(imageUri) ?: return BaseResult.Error(
            ApiError.unknownError("이미지 파일을 읽을 수 없습니다.")
        )

        Timber.d("[ProfileImage] multipart created | partName=${filePart.headers}")

        val uploadResult = safeApiCall {
            userApiService.uploadProfileImage(filePart)
        }

        return when (uploadResult) {
            is BaseResult.Success -> {
                Timber.d("[ProfileImage] upload succeeded | imageUrl=${uploadResult.data}")
                getMyProfile()
            }
            is BaseResult.Error -> {
                Timber.e("[ProfileImage] upload failed | code=${uploadResult.error.code} | message=${uploadResult.error.message}")
                uploadResult
            }
        }
    }

    override suspend fun deleteProfileImage(): BaseResult<UserProfile> {
        val deleteResult = safeApiCallWithoutData {
            userApiService.deleteProfileImage()
        }

        return when (deleteResult) {
            is BaseResult.Success -> getMyProfile()
            is BaseResult.Error -> deleteResult
        }
    }

    override suspend fun getMyDataAccepted(): BaseResult<Boolean> {
        return safeApiCall {
            userApiService.getMyDataAccepted()
        }
    }

    override suspend fun acceptMyData(): BaseResult<Unit> {
        return safeApiCallWithoutData {
            userApiService.acceptMyData()
        }
    }

    override suspend fun getMyDataHoldings(): BaseResult<List<MyDataHolding>> {
        return safeApiCall {
            userApiService.getMyDataHoldings()
        }.map { response ->
            response.map { it.toDomain() }
        }
    }

    private fun buildProfileImagePart(imageUri: String): MultipartBody.Part? {
        val uri = Uri.parse(imageUri)
        val resolver = context.contentResolver
        val bytes = compressProfileImage(uri) ?: return null
        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val fileName = queryFileName(uri)
            ?.substringBeforeLast('.', missingDelimiterValue = queryFileName(uri) ?: "profile_image")
            ?.plus(".jpg")
            ?: "profile_image.jpg"
        Timber.d("[ProfileImage] compressed | uploadSize=${bytes.size}")
        return MultipartBody.Part.createFormData("file", fileName, requestBody)
    }

    private fun queryFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

    private fun queryFileSize(uri: Uri): Long? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) cursor.getLong(sizeIndex) else null
        }
    }

    private fun validateProfileImage(imageUri: String): BaseResult<Unit> {
        val uri = Uri.parse(imageUri)
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri).orEmpty().lowercase()
        val fileName = queryFileName(uri).orEmpty().lowercase()
        val fileSize = queryFileSize(uri)

        Timber.d("[ProfileImage] validate | mimeType=$mimeType | fileName=$fileName | fileSize=$fileSize")

        val isSupportedMime = mimeType.startsWith("image/")
        val isSupportedExtension = SUPPORTED_IMAGE_EXTENSIONS.any { fileName.endsWith(it) }

        return when {
            !isSupportedMime && !isSupportedExtension -> BaseResult.Error(
                ApiError.unknownError("이미지 파일만 업로드할 수 있습니다.")
            )
            else -> BaseResult.Success(Unit)
        }
    }

    private fun compressProfileImage(uri: Uri): ByteArray? {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longestEdge = maxOf(info.size.width, info.size.height)
            if (longestEdge > MAX_PROFILE_IMAGE_DIMENSION) {
                val ratio = MAX_PROFILE_IMAGE_DIMENSION.toFloat() / longestEdge.toFloat()
                decoder.setTargetSize(
                    (info.size.width * ratio).toInt().coerceAtLeast(1),
                    (info.size.height * ratio).toInt().coerceAtLeast(1)
                )
            }
        }

        return bitmap.useCompressedJpeg(
            maxBytes = MAX_PROFILE_IMAGE_BYTES,
            initialQuality = 90
        )
    }

    private fun Bitmap.useCompressedJpeg(
        maxBytes: Long,
        initialQuality: Int
    ): ByteArray? {
        var quality = initialQuality
        var currentBitmap = this

        while (quality >= MIN_PROFILE_IMAGE_QUALITY) {
            val output = java.io.ByteArrayOutputStream()
            output.use { stream ->
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                val bytes = stream.toByteArray()
                if (bytes.size <= maxBytes) {
                    if (currentBitmap !== this) currentBitmap.recycle()
                    if (!isRecycled) recycle()
                    return bytes
                }
            }

            quality -= PROFILE_IMAGE_QUALITY_STEP
        }

        var scaledBitmap = currentBitmap
        while (scaledBitmap.width > MIN_PROFILE_IMAGE_DIMENSION && scaledBitmap.height > MIN_PROFILE_IMAGE_DIMENSION) {
            val nextWidth = (scaledBitmap.width * PROFILE_IMAGE_SCALE_FACTOR).toInt().coerceAtLeast(MIN_PROFILE_IMAGE_DIMENSION)
            val nextHeight = (scaledBitmap.height * PROFILE_IMAGE_SCALE_FACTOR).toInt().coerceAtLeast(MIN_PROFILE_IMAGE_DIMENSION)
            val resized = Bitmap.createScaledBitmap(scaledBitmap, nextWidth, nextHeight, true)
            if (scaledBitmap !== this) scaledBitmap.recycle()
            scaledBitmap = resized

            val output = java.io.ByteArrayOutputStream()
            output.use { stream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, MIN_PROFILE_IMAGE_QUALITY, stream)
                val bytes = stream.toByteArray()
                if (bytes.size <= maxBytes) {
                    scaledBitmap.recycle()
                    if (!isRecycled) recycle()
                    return bytes
                }
            }
        }

        if (scaledBitmap !== this) scaledBitmap.recycle()
        if (!isRecycled) recycle()
        return null
    }

    companion object {
        private const val MAX_PROFILE_IMAGE_BYTES = 5 * 1024 * 1024L
        private val SUPPORTED_IMAGE_EXTENSIONS = setOf(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp"
        )
        private const val MAX_PROFILE_IMAGE_DIMENSION = 1600
        private const val MIN_PROFILE_IMAGE_DIMENSION = 400
        private const val MIN_PROFILE_IMAGE_QUALITY = 55
        private const val PROFILE_IMAGE_QUALITY_STEP = 10
        private const val PROFILE_IMAGE_SCALE_FACTOR = 0.8f
    }
}
