import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Animated,
} from 'react-native';
import CompassModule, { compassEmitter } from '../types/CompassModule';

const CompassTracker: React.FC = () => {
  const [azimuth, setAzimuth] = useState(0);
  const [steps, setSteps] = useState(0);
  const [isTracking, setIsTracking] = useState(false);
  const rotateAnim = new Animated.Value(0);

  useEffect(() => {
    const compassSubscription = compassEmitter.addListener(
      'CompassUpdate',
      (event) => {
        setAzimuth(event.azimuth);
        rotateAnim.setValue(event.azimuth);
      }
    );

    const stepSubscription = compassEmitter.addListener(
      'StepUpdate',
      (event) => {
        setSteps(event.steps);
      }
    );

    return () => {
      compassSubscription.remove();
      stepSubscription.remove();
    };
  }, []);

  const toggleTracking = () => {
    if (isTracking) {
      CompassModule.stopCompass();
      CompassModule.stopStepCounter();
    } else {
      CompassModule.startCompass();
      CompassModule.startStepCounter();
    }
    setIsTracking(!isTracking);
  };

  const spin = rotateAnim.interpolate({
    inputRange: [0, 360],
    outputRange: ['0deg', '360deg'],
  });

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Compass Tracker</Text>
      </View>

      <View style={styles.compassContainer}>
        <Animated.View
          style={[
            styles.compass,
            {
              transform: [{ rotate: spin }],
            },
          ]}
        >
          <Text style={styles.arrow}>↑</Text>
        </Animated.View>
        <Text style={styles.degree}>{Math.round(azimuth)}°</Text>
      </View>

      <View style={styles.stepsContainer}>
        <Text style={styles.stepsText}>{Math.round(steps)} steps</Text>
      </View>

      <TouchableOpacity
        style={[styles.button, isTracking && styles.buttonActive]}
        onPress={toggleTracking}
      >
        <Text style={styles.buttonText}>
          {isTracking ? 'Stop Tracking' : 'Start Tracking'}
        </Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 30,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  compassContainer: {
    alignItems: 'center',
    marginBottom: 30,
  },
  compass: {
    marginBottom: 20,
  },
  arrow: {
    fontSize: 60,
    color: '#2196F3',
  },
  degree: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#2196F3',
  },
  stepsContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 30,
  },
  stepsText: {
    fontSize: 24,
    color: '#4CAF50',
  },
  button: {
    backgroundColor: '#2196F3',
    padding: 15,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonActive: {
    backgroundColor: '#f44336',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
});

export default CompassTracker; 