import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

interface CompassModuleInterface {
  startCompass(): void;
  stopCompass(): void;
  startStepCounter(): void;
  stopStepCounter(): void;
}

const { CompassModule } = NativeModules;

// Check if module exists
if (!CompassModule) {
  console.error('CompassModule is not available. Please check if the native module is properly linked.');
}

const compassEmitter = new NativeEventEmitter(CompassModule || {});

// Create a wrapper with error logging
const wrappedModule = {
  startCompass: () => {
    if (!CompassModule) {
      console.error('Cannot start compass: Module not available');
      return;
    }
    CompassModule.startCompass();
  },
  stopCompass: () => {
    if (!CompassModule) {
      console.error('Cannot stop compass: Module not available');
      return;
    }
    CompassModule.stopCompass();
  },
  startStepCounter: () => {
    if (!CompassModule) {
      console.error('Cannot start step counter: Module not available');
      return;
    }
    CompassModule.startStepCounter();
  },
  stopStepCounter: () => {
    if (!CompassModule) {
      console.error('Cannot stop step counter: Module not available');
      return;
    }
    CompassModule.stopStepCounter();
  }
};

export default wrappedModule as CompassModuleInterface;
export { compassEmitter }; 