package com.example.toolsapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategorySpacer(title: String, topPadding: Int = 32){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .padding(top = topPadding.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
        ){

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(8.dp)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.onSurface)
        )

        Text(
            color = MaterialTheme.colorScheme.onSurface,
            text = title
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(8.dp)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}