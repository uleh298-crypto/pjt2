package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.*

/**
 * 검색 바
 *
 * 탐색 화면 상단 등에서 사용하는 검색 입력 필드.
 *
 * @param query           현재 검색어
 * @param onQueryChange   검색어 변경 콜백
 * @param onSearch        키보드 검색(완료) 콜백
 * @param onClear         X 버튼 클릭 콜백
 * @param placeholder     힌트 텍스트
 */
@Composable
fun WyeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {},
    onClear: () -> Unit = { onQueryChange("") },
    placeholder: String = "ETF 검색",
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
        ),
        cursorBrush = SolidColor(PrimaryGreen),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        modifier = modifier.focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "검색",
                    tint = if (query.isEmpty()) NavInactive else PrimaryGreen,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(text = placeholder, color = TextHint, fontSize = 15.sp)
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "지우기",
                            tint = NavInactive,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        },
    )
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun WyeSearchBarPreview() {
    var query by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WyeSearchBar(query = "", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
        WyeSearchBar(query = "KODEX 200", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}
