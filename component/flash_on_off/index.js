import React, { useState, memo } from 'react'
import styled from 'styled-components'
import { Image, TouchableOpacity } from 'react-native'
import { ImgUrl } from '../../global/img_url';
import PropTypes from 'prop-types'

export const FlashConstant = {
    INIT: 'init',
    ON: 'on',
    OFF: 'off'
}

export const FlashOnOff = memo(({ ...props }) => {

    const [isFlash, setFlash] = useState(props.flash)

    function pressFlash() {
        var newFlash = FlashConstant.INIT
        if (isFlash == FlashConstant.INIT || isFlash == FlashConstant.OFF)
            newFlash = FlashConstant.ON
        else
            newFlash = FlashConstant.OFF

        setFlash(newFlash)

        if (props.onClickFlash != null)
            props.onClickFlash(newFlash)
    }

    return (
        <TouchableOpacity style={props.style} onPress={pressFlash}>
            <Img source={isFlash == FlashConstant.ON ? ImgUrl.FLASH : ImgUrl.FLASH_ABORT} />
        </TouchableOpacity>
    )
})

const Img = styled(Image)`
    width: 26;
    height: 26;
`

FlashOnOff.defaultProps = {
    flash: FlashConstant.INIT
}

FlashOnOff.propTypes = {
    flash: PropTypes.string
}