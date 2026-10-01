package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


@Composable
internal fun TopIconBottomLabel(
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        icon()
        label(Modifier)
    }
}

@Composable
internal fun TopLabelBottomIcon(
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        label(Modifier)
        icon()
    }
}

@Composable
internal fun StartIconEndLabel(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        icon()
        label(Modifier)
    }
}

@Composable
internal fun StartLabelEndIcon(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        label(Modifier)
        icon()
    }
}

@Composable
internal fun IconOnly(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        icon()
    }
}

@Composable
internal fun LabelOnly(
    modifier: Modifier = Modifier,
    iconSize: Dp,
    iconPadding: Dp,
    iconModifier: Modifier,
    label: @Composable (Modifier) -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .padding(iconPadding),
        ) {
            label(
                Modifier
                    .matchParentSize()
                    .then(iconModifier),
            )
        }
    }
}