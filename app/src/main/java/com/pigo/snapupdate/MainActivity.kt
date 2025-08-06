package com.pigo.snapupdate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.pigo.snapupdate.ui.screens.HomeScreen
import com.pigo.snapupdate.ui.theme.SnapUpdateTheme
import com.pigo.snapupdate.ui.viewmodel.UpdateUiState
import com.pigo.snapupdate.ui.viewmodel.UpdateViewModel
import com.pigo.snapupdate.utils.PermissionManager
import androidx.fragment.app.FragmentActivity
class MainActivity : FragmentActivity() {
    
    private lateinit var permissionManager: PermissionManager
    private lateinit var viewModel: UpdateViewModel
    
    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        permissionManager.onActivityResult(
            PermissionManager.REQUEST_INSTALL_PACKAGES_PERMISSION,
            result.resultCode
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize permission manager
        permissionManager = PermissionManager(this)
        
        setContent {
            SnapUpdateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    viewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(UpdateViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return UpdateViewModel.create(this@MainActivity as Context) as T
                                }
                                throw IllegalArgumentException("Unknown ViewModel class")
                            }
                        }
                    )
                    val uiState by viewModel.uiState.collectAsState()
                    
                    HomeScreen(
                        uiState = uiState,
                        onCheckUpdate = { 
                            // Check permissions before checking for updates
                            if (permissionManager.checkAndRequestPermissions(this)) {
                                viewModel.checkForUpdates() 
                            }
                        },
                        onAcceptUpdate = { 
                            // Check permissions before accepting update
                            if (permissionManager.checkAndRequestPermissions(this)) {
                                viewModel.acceptUpdate() 
                            }
                        },
                        onDismissUpdate = { viewModel.dismissUpdate() },
                        onDismissInstall = { viewModel.dismissInstallDialog() },
                        onIncrementVersion = { viewModel.incrementVersion() },
                        onResetVersion = { viewModel.resetVersion() }
                    )
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            PermissionManager.REQUEST_INSTALL_PACKAGES_PERMISSION -> {
                if (resultCode == RESULT_OK) {
                    // Permission granted, retry the action
                    viewModel.checkForUpdates()
                }
            }
        }
    }
}