import PropTypes from 'prop-types';
import { requireNativeComponent, StyleSheet } from 'react-native'
import React from 'react'
const BarCodeCameraView = requireNativeComponent('BarCodeCameraView')

export const BarCodeCamera = props => {
    return (
        <BarCodeCameraView
            {...props}
        />
    )
}

export const BarCodeCameraType = {
    AZTEC: 'AZTEC',
    CODEBAR: 'CODABAR',
    CODE_39: 'CODE_39',
    CODE_93: 'CODE_93',
    CODE_128: 'CODE_128',
    DATA_MATRIX: 'DATA_MATRIX',
    EAN_8: 'EAN8',
    EAN_13: 'EAN13',
    ITF: 'INTERLEAVED2OF5',
    MAXICODE: 'MAXICODE',
    PDF_47: 'PDF_47',
    QR: 'QR',
    QR_CODE: 'QR_CODE',
    RSS_14: 'RSS_14',
    RSS_EXPANDED: 'RSSEXPANDED',
    UPC_A: 'UPC_A',
    UPC_E: 'UPC_E',
    UPC_EAN: 'UPC_EAN'
}

BarCodeCamera.defaultProp = {
    barCodeTypes: []
}

BarCodeCamera.propTypes = {
    barCodeTypes: PropTypes.arrayOf(PropTypes.string)
}