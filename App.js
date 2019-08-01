import React from 'react';
import { createAppContainer } from 'react-navigation'
import { Scan } from './screen/scan';
import { AppNavigation } from './navigation/app_navigation';
import { Navigation } from './navigation/navigation'

const AppContainer = createAppContainer(AppNavigation)

const App = () => {
  return <AppContainer ref={node => Navigation.setTopLevelNavigator(node)} />
}

export default App;
