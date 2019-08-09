import { createStackNavigator } from 'react-navigation'
import { Scan } from '../screen/scan';
import { ScanCamera } from '../screen/scan_camera';
import { Stack } from './navigation';

const MainStack = createStackNavigator({
    [Stack.Scan]: { screen: Scan },
    [Stack.ScanCamera]: { screen: ScanCamera },
}, { headerMode: 'none' })

const AppNavigation = MainStack

export { AppNavigation }