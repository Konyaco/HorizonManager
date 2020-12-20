package org.wvt.horizonmgr.ui.login

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wvt.horizonmgr.ui.components.FabState
import org.wvt.horizonmgr.ui.components.StateFab

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RegisterPage(
    fabState: FabState,
    onRegisterRequest: (username: String, email: String, password: String, confirmPassword: String) -> Unit
) {
    var username by savedInstanceState(saver = TextFieldValue.Saver) {
        TextFieldValue()
    }
    val usernameFocus = remember { FocusRequester() }
    var email by savedInstanceState(saver = TextFieldValue.Saver) {
        TextFieldValue()
    }
    val emailFocus = remember { FocusRequester() }
    var password by savedInstanceState(saver = TextFieldValue.Saver) {
        TextFieldValue()
    }
    val passwordFocus = remember { FocusRequester() }
    var confirmPassword by savedInstanceState(saver = TextFieldValue.Saver) {
        TextFieldValue()
    }
    val confirmFocus = remember { FocusRequester() }

    Box(Modifier.fillMaxSize()) {
        ScrollableColumn(
            modifier = Modifier.wrapContentWidth().fillMaxHeight().align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {
            Text("注册", color = MaterialTheme.colors.primary, fontSize = 64.sp)
            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "注册到 InnerCore 中文社区 ",
                )
            }
            Column(
                modifier = Modifier.wrapContentWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                TextField(
                    modifier = Modifier.focusRequester(usernameFocus),
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    onImeActionPerformed = { imeAction, softwareKeyboardController ->
                        softwareKeyboardController?.hideSoftwareKeyboard()
                        usernameFocus.freeFocus()
                        emailFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusRequester(emailFocus),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("邮箱") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    onImeActionPerformed = { imeAction, softwareKeyboardController ->
                        softwareKeyboardController?.hideSoftwareKeyboard()
                        emailFocus.freeFocus()
                        passwordFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusRequester(passwordFocus),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    onImeActionPerformed = { imeAction, softwareKeyboardController ->
                        softwareKeyboardController?.hideSoftwareKeyboard()
                        passwordFocus.freeFocus()
                        confirmFocus.requestFocus()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    modifier = Modifier.focusRequester(confirmFocus),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("重复密码") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    onImeActionPerformed = { imeAction, softwareKeyboardController ->
                        softwareKeyboardController?.hideSoftwareKeyboard()
                        confirmFocus.freeFocus()
                        onRegisterRequest(
                            username.text,
                            email.text,
                            password.text,
                            confirmPassword.text
                        )
                    }
                )
                StateFab(
                    modifier = Modifier.padding(top = 16.dp),
                    state = fabState, onClicked = {
                        onRegisterRequest(
                            username.text,
                            email.text,
                            password.text,
                            confirmPassword.text
                        )
                    }
                )
            }
        }
    }
}
