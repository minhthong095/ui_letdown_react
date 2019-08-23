import { createStackNavigator } from 'react-navigation'
import { Scan } from '../screen/scan';
import { ScanCamera } from '../screen/scan_camera';
import { Stack } from './navigation';
import { ScanCameraLibrary } from '../screen/scan-camera-library';
import { ScanCameraRegion } from '../screen/scan_camera_region';

const MainStack = createStackNavigator({
    [Stack.Scan]: { screen: ScanCameraRegion },
    [Stack.ScanCamera]: { screen: ScanCameraRegion },
}, { headerMode: 'none' })

const AppNavigation = MainStack

export { AppNavigation }