#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../include/main_java_BLEServer.h"

/*
 * Template pour l'implémentation Linux BLE Server
 * Vous devrez implémenter les fonctions BLE réelles avec BlueZ ou une autre bibliothèque
 */

/*
 * Class:     main_java_BLEServer
 * Method:    nativeStartServer
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_main_java_BLEServer_nativeStartServer
  (JNIEnv *env, jobject obj, jstring serviceUuid, jstring charUuid) {
    
    // TODO: Implémenter le démarrage du serveur BLE avec BlueZ
    // Pour l'instant, simuler un échec
    printf("BLEServer Linux: nativeStartServer appelé\n");
    
    // Convertir les strings Java
    const char *serviceStr = (*env)->GetStringUTFChars(env, serviceUuid, 0);
    const char *charStr = (*env)->GetStringUTFChars(env, charUuid, 0);
    
    printf("Service UUID: %s\n", serviceStr);
    printf("Characteristic UUID: %s\n", charStr);
    
    // Libérer les strings
    (*env)->ReleaseStringUTFChars(env, serviceUuid, serviceStr);
    (*env)->ReleaseStringUTFChars(env, charUuid, charStr);
    
    // Retourner 0 pour succès, -1 pour échec
    return -1; // Échec pour l'instant
}

/*
 * Class:     main_java_BLEServer
 * Method:    nativeStopServer
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_main_java_BLEServer_nativeStopServer
  (JNIEnv *env, jobject obj) {
    
    // TODO: Implémenter l'arrêt du serveur BLE
    printf("BLEServer Linux: nativeStopServer appelé\n");
}

/*
 * Class:     main_java_BLEServer
 * Method:    nativeNotify
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_main_java_BLEServer_nativeNotify
  (JNIEnv *env, jobject obj, jbyteArray data) {
    
    // TODO: Implémenter l'envoi de notification BLE
    printf("BLEServer Linux: nativeNotify appelé\n");
    
    // Obtenir les données
    jsize len = (*env)->GetArrayLength(env, data);
    jbyte *body = (*env)->GetByteArrayElements(env, data, 0);
    
    printf("Données à envoyer (%d bytes): ", len);
    for (int i = 0; i < len && i < 10; i++) {
        printf("%02X ", (unsigned char)body[i]);
    }
    printf("\n");
    
    // Libérer les données
    (*env)->ReleaseByteArrayElements(env, data, body, 0);
    
    // Retourner 0 pour succès, -1 pour échec
    return -1; // Échec pour l'instant
}