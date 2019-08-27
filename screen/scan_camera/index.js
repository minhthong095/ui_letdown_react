import React, { useState } from 'react'
import styled from 'styled-components'
import PropTypes from 'prop-types'
import { View, Image, TouchableOpacity, StyleSheet, PixelRatio, Alert } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import { FlashOnOff } from '../../component/flash_on_off';
import { CropRegion } from '../../component/crop_region';
import { MockCamera } from '../../component/mock_camera';
import { Navigation } from '../../navigation/navigation';
import { BarCodeCamera } from '../../component/bar_code_camera';

export const ScanCamera = props => {

    const [dimensionContainer, setDimensionContainer] = useState({ widthContainer: undefined, heightContainer: undefined })

    const onContainerLayout = event => {
        const { width, height } = event.nativeEvent.layout;
        const { widthCrop, heightCrop, yCrop } = props

        if (widthCrop > width)
            throw Error("`widthCrop` is inappropriate.")
        else if (heightCrop + yCrop > height)
            throw Error("Both `widthCrop` and `yCrop` are inappropriate.")
        else
            setDimensionContainer({ widthContainer: width, heightContainer: height })
    }

    const getCropData = (widthCrop, heightCrop, yCrop, xCrop) => {
        return (
            PixelRatio.getPixelSizeForLayoutSize(xCrop) + ","
            + PixelRatio.getPixelSizeForLayoutSize(yCrop) + ","
            + PixelRatio.getPixelSizeForLayoutSize(widthCrop) + ","
            + PixelRatio.getPixelSizeForLayoutSize(heightCrop)
        )
    }

    const { widthCrop, heightCrop, yCrop } = props
    const xCrop = (dimensionContainer.widthContainer - widthCrop) / 2

    return (
        <Container onLayout={onContainerLayout}>
            {
                dimensionContainer.widthContainer === undefined ? null :
                    <Container>
                        <BarCodeCamera
                            // onBarCodeRead={result => {
                            //     console.log('Result: ' + result)
                            //     Navigation.stackPop()
                            //     Alert.alert(result)
                            // }}
                            cropData={getCropData(widthCrop, heightCrop, yCrop, xCrop)}
                            style={StyleSheet.absoluteFill} />
                        <CropRegion
                            widthContainer={dimensionContainer.widthContainer}
                            heightContainer={dimensionContainer.heightContainer}
                            widthCrop={widthCrop}
                            heightCrop={heightCrop}
                            xCrop={xCrop}
                            yCrop={yCrop}
                        />
                        <TouchableOpacityAbort onPress={_ => { Navigation.stackPop() }}>
                            <AbortX source={ImgUrl.ABORT_X} />
                        </TouchableOpacityAbort>
                        <Flash />
                    </Container>
            }
        </Container>
    )
}

const TouchableOpacityAbort = styled(TouchableOpacity)`
    top: 20;
    left: 20;
    position: absolute;
`

const Flash = styled(FlashOnOff)`
    position: absolute;
    top: 20;
    right: 20;
`

const AbortX = styled(Image)`
    width: 39;
    height: 39
`
const Container = styled(View)`
    flex: 1;
    flex-direction: row;
    background-color: black;
`

ScanCamera.propTypes = {
    widthCrop: PropTypes.number,
    heightCrop: PropTypes.number,
    yCrop: PropTypes.number
}

ScanCamera.defaultProps = {
    widthCrop: 220,
    heightCrop: 200,
    yCrop: 100
}