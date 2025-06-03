// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.appDataStorage: DataStore<Preferences> by preferencesDataStore(name = "app_data")
val Context.scannedTicketStorage: DataStore<Preferences> by preferencesDataStore(name = "scanned_tickets")

class DataStoreUtils {
    companion object {
        suspend fun getString(datastore: DataStore<Preferences>, key: String): String? {
            Log.d("DataStoreUtils", "getString($datastore, $key) called")

            val valueFlow: Flow<String?> = datastore.data.map { data ->
                data[stringPreferencesKey(key)]
            }

            return valueFlow.first()
        }

        suspend fun getBoolean(datastore: DataStore<Preferences>, key: String): Boolean? {
            Log.d("DataStoreUtils", "getBoolean($datastore, $key) called")

            val valueFlow: Flow<Boolean?> = datastore.data.map { data ->
                data[booleanPreferencesKey(key)]
            }

            return valueFlow.first()
        }

        suspend fun setString(datastore: DataStore<Preferences>, key: String, value: String) {
            Log.d("DataStoreUtils", "setString($datastore, $key) called")

            datastore.edit { data ->
                data[stringPreferencesKey(key)] = value
            }
        }

        suspend fun setBoolean(datastore: DataStore<Preferences>, key: String, value: Boolean) {
            Log.d("DataStoreUtils", "setBoolean($datastore, $key) called")

            datastore.edit { data ->
                data[booleanPreferencesKey(key)] = value
            }
        }

        suspend fun removeString(datastore: DataStore<Preferences>, key: String) {
            Log.d("DataStoreUtils", "removeString($datastore, $key) called")

            datastore.edit { data ->
                data.remove(stringPreferencesKey(key))
            }
        }

        suspend fun removeBoolean(datastore: DataStore<Preferences>, key: String) {
            Log.d("DataStoreUtils", "removeBoolean($datastore, $key) called")

            datastore.edit { data ->
                data.remove(booleanPreferencesKey(key))
            }
        }

        suspend fun clear(datastore: DataStore<Preferences>) {
            Log.d("DataStoreUtils", "clear($datastore) called")

            datastore.edit { data ->
                data.clear()
            }
        }
    }
}