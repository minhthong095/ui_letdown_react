import React, { useState, memo } from 'react'
import styled from 'styled-components'
import { Image, TouchableOpacity } from 'react-native'
import { ImgUrl } from '../../global/img_url';

export const FlashOnOff = memo(({ ...props }) => {

    const [isFlash, setFlash] = useState(false)

    function pressFlash() {
        setFlash(!isFlash)
        if (props.onClickFlash != null)
            props.onClickFlash(!isFlash)
    }

    return (
        <TouchableOpacity style={props.style} onPress={pressFlash}>
            <Img source={isFlash ? ImgUrl.FLASH : ImgUrl.FLASH_ABORT} />
        </TouchableOpacity>
    )
})

const Img = styled(Image)`
    width: 29;
    height: 29;
`