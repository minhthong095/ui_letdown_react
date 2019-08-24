import React from 'react'
import { View } from 'react-native'
import styled from 'styled-components'
import PropTypes from 'prop-types'

export const CornerProtect = props => {
    const { type, width, height, colorStyle, cornerWidth, style } = props

    rotate = '0deg'

    if (type === 'topRight')
        rotate = '90deg'
    else if (type === 'bottomRight')
        rotate = '180deg'
    else if (type === 'bottomLeft')
        rotate = '270deg'

    return (
        <View style={style}>
            <Container style={{ transform: [{ rotate: rotate }] }} width={width} height={height}>
                <UpDraw width={width} cornerWidth={cornerWidth} colorStyle={colorStyle} />
                <DownDraw height={height} cornerWidth={cornerWidth} colorStyle={colorStyle} />
            </Container>
        </View>
    )
}

const Container = styled(View)`
    width: ${props => props.width}
    height: ${props => props.height}
`

const UpDraw = styled(View)`
    position: absolute;
    width: ${props => props.width}
    height: ${props => props.cornerWidth};
    background-color: ${props => props.colorStyle}
`

const DownDraw = styled(View)`
    position: absolute;
    width: ${props => props.cornerWidth};
    height: ${props => props.height}
    background-color: ${props => props.colorStyle}
`

CornerProtect.defaultProps = {
    type: 'topLeft',
    width: 11,
    height: 11,
    colorStyle: 'white',
    cornerWidth: 2
}

CornerProtect.propTypes = {
    type: PropTypes.string,
    width: PropTypes.number,
    height: PropTypes.number,
    colorStyle: PropTypes.string,
    cornerWidth: PropTypes.number
}