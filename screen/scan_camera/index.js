import React, { useState, useRef, useCallback, useEffect, useLayoutEffect } from 'react'
import styled from 'styled-components'
import PropTypes from 'prop-types'
import { View, Text, SafeAreaView, Image, TouchableOpacity, Platform, PixelRatio, Alert, findNodeHandle } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import { FlashOnOff, FlashConstant } from '../../component/flash_on_off';
import { CropRegion } from '../../component/crop_region';
import { Navigation } from '../../navigation/navigation';
import { BarCodeCamera } from '../../component/bar_code_camera';

export const ScanCamera = props => {

    const [dimensionContainer, setDimensionContainer] = useState({ widthContainer: undefined, heightContainer: undefined })
    const [flashState, setFlash] = useState(FlashConstant.INIT)
    const refLock = useRef(false)
    const refFuncCamera = useRef(null)

    function onContainerLayout(event) {
        const { width, height } = event.nativeEvent.layout;
        const { widthCrop, heightCrop, yCrop } = props

        if (widthCrop > width)
            throw Error("`widthCrop` is inappropriate.")
        else if (heightCrop + yCrop > height)
            throw Error("Both `widthCrop` and `yCrop` are inappropriate.")
        else
            setDimensionContainer({ widthContainer: width, heightContainer: height })
    }

    function _getCropData(widthCrop, heightCrop, yCrop, xCrop) {
        return (
            PixelRatio.getPixelSizeForLayoutSize(xCrop) + ","
            + PixelRatio.getPixelSizeForLayoutSize(yCrop) + ","
            + PixelRatio.getPixelSizeForLayoutSize(widthCrop) + ","
            + PixelRatio.getPixelSizeForLayoutSize(heightCrop)
        )
    }

    function _onClickFlash(flashState) {
        setFlash(flashState)
    }

    const onTouchCrop = useCallback(_ => {
        refFuncCamera.current()
    })

    const { widthCrop, heightCrop, yCrop } = props
    const xCrop = (dimensionContainer.widthContainer - widthCrop) / 2

    return (
        <Container onLayout={onContainerLayout}>
            {
                dimensionContainer.widthContainer === undefined ? null :
                    <Container>
                        <BarCodeCamera
                            receiveTouchCropFunc={funcTouchCrop => refFuncCamera.current = funcTouchCrop}
                            onBarCodeRead={result => {
                                if (!refLock.current) {
                                    refLock.current = true
                                    Alert.alert("", result)
                                    Navigation.stackPop()
                                }
                            }}
                            flash={flashState}
                            cropData={_getCropData(widthCrop, heightCrop, yCrop, xCrop)}
                        />
                        {
                            Platform.OS == "ios" ?
                                <ContainerButtonJs>
                                    <TouchableButtonJs onPress={_ => onTouchCrop()}>
                                        <TextJs>Touch Crop from JS</TextJs>
                                    </TouchableButtonJs>
                                </ContainerButtonJs>
                                :
                                <CropRegion
                                    onTouchCrop={onTouchCrop}
                                    widthContainer={dimensionContainer.widthContainer}
                                    heightContainer={dimensionContainer.heightContainer}
                                    widthCrop={widthCrop}
                                    heightCrop={heightCrop}
                                    xCrop={xCrop}
                                    yCrop={yCrop}
                                />
                        }
                        <TouchableOpacityAbort onPress={_ => { Navigation.stackPop() }}>
                            <AbortX source={ImgUrl.ABORT_X} />
                        </TouchableOpacityAbort>
                        <Flash flash={flashState} onClickFlash={_onClickFlash} />
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
    width: 36;
    height: 36
`
const Container = styled(SafeAreaView)`
    flex: 1;
    flex-direction: row;
    background-color: black;
`

const TextJs = styled(Text)`
    color: white;
    padding: 20px;
    font-size: 18;
`

const TouchableButtonJs = styled(TouchableOpacity)`
    background-color: red;
`
const ContainerButtonJs = styled(View)`
    width: 100%;
    align-items: center;
    position: absolute;
    bottom: 0;
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