package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.Background
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextHint
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.WYETheme

enum class WyeTextFieldStyle {
    Filled,
    Underlined
}

/**
 * 텍스트 필드 — 범용적인 텍스트 필드
 */
@Composable
fun WyeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    style: WyeTextFieldStyle = WyeTextFieldStyle.Filled
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        singleLine = singleLine,
        shape = if (style == WyeTextFieldStyle.Filled) {
            RoundedCornerShape(12.dp)
        } else {
            RoundedCornerShape(0.dp)
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = TextHint
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                if (onTrailingIconClick != null) {
                    IconButton(onClick = onTrailingIconClick) {
                        Icon(
                            imageVector = it,
                            contentDescription = trailingIconDescription,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = it,
                        contentDescription = trailingIconDescription,
                        tint = TextSecondary
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = if (style == WyeTextFieldStyle.Filled) Background else Color.Transparent,
            unfocusedContainerColor = if (style == WyeTextFieldStyle.Filled) Background else Color.Transparent,
            disabledContainerColor = if (style == WyeTextFieldStyle.Filled) Background else Color.Transparent,
            focusedIndicatorColor = if (style == WyeTextFieldStyle.Filled) Color.Transparent else PrimaryGreen,
            unfocusedIndicatorColor = if (style == WyeTextFieldStyle.Filled) Color.Transparent else Divider,
            disabledIndicatorColor = if (style == WyeTextFieldStyle.Filled) Color.Transparent else Divider,
            focusedLeadingIconColor = PrimaryGreen,
            unfocusedLeadingIconColor = TextSecondary,
            focusedTrailingIconColor = TextSecondary,
            unfocusedTrailingIconColor = TextSecondary
        )
    )
}

/**
 * 닉네임 텍스트 필드
 */
@Composable
fun WyeNicknameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "닉네임을 입력하세요.",
    style: WyeTextFieldStyle = WyeTextFieldStyle.Filled
) {
    WyeTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Person,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier,
        style = style
    )
}

/**
 * 이메일 텍스트 필드
 */
@Composable
fun WyeEmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "user@example.com",
    style: WyeTextFieldStyle = WyeTextFieldStyle.Filled
) {
    WyeTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Email,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = modifier,
        style = style
    )
}

/**
 * 비밀번호 텍스트 필드
 */
@Composable
fun WyePasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "8자 이상 입력하세요",
    style: WyeTextFieldStyle = WyeTextFieldStyle.Filled
) {
    WyeTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Lock,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = if (isVisible) {
            Icons.Filled.VisibilityOff
        } else {
            Icons.Filled.Visibility
        },
        trailingIconDescription = if (isVisible) {
            "비밀번호 숨기기"
        } else {
            "비밀번호 보기"
        },
        onTrailingIconClick = onVisibilityToggle,
        modifier = modifier,
        style = style
    )
}

/**
 * 비밀번호 재확인 텍스트 필드
 */
@Composable
fun WyePasswordConfirmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "비밀번호를 다시 입력하세요",
    style: WyeTextFieldStyle = WyeTextFieldStyle.Filled
) {
    WyePasswordTextField(
        value = value,
        onValueChange = onValueChange,
        isVisible = isVisible,
        onVisibilityToggle = onVisibilityToggle,
        modifier = modifier,
        placeholder = placeholder,
        style = style
    )
}

/**
 * 인증번호 텍스트 필드
 */
@Composable
fun WyeVerificationCodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "인증번호 6자리를 입력하세요",
    style: WyeTextFieldStyle = WyeTextFieldStyle.Filled
) {
    WyeTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        style = style
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun WyeTextFieldPreview() {
    WYETheme {
        val (nickname, setNickname) = remember { mutableStateOf("") }
        val (email, setEmail) = remember { mutableStateOf("user@example.com") }
        val (password, setPassword) = remember { mutableStateOf("password123!") }
        val (passwordConfirm, setPasswordConfirm) = remember { mutableStateOf("") }
        val (code, setCode) = remember { mutableStateOf("123456") }
        val (passwordVisible, setPasswordVisible) = remember { mutableStateOf(false) }
        val (passwordConfirmVisible, setPasswordConfirmVisible) = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .background(SurfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WyeNicknameTextField(
                value = nickname,
                onValueChange = setNickname
            )
            WyeEmailTextField(
                value = email,
                onValueChange = setEmail
            )
            WyeVerificationCodeTextField(
                value = code,
                onValueChange = setCode
            )
            WyePasswordTextField(
                value = password,
                onValueChange = setPassword,
                isVisible = passwordVisible,
                onVisibilityToggle = { setPasswordVisible(!passwordVisible) }
            )
            WyePasswordConfirmTextField(
                value = passwordConfirm,
                onValueChange = setPasswordConfirm,
                isVisible = passwordConfirmVisible,
                onVisibilityToggle = { setPasswordConfirmVisible(!passwordConfirmVisible) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
