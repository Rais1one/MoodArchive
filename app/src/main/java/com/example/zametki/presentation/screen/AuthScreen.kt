package com.example.zametki.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    fun validateEmail(input: String): String? {
        return when {
            input.isEmpty() -> "Введи email"
            input.contains(" ") -> "Email не должен содержать пробелы"
            !input.contains("@") -> "Неверный формат email — нет символа @"
            input.startsWith("@") -> "Неверный формат email"
            input.endsWith("@") -> "Неверный формат email"
            input.startsWith(".") -> "Email не может начинаться с точки"
            input.endsWith(".") -> "Email не может заканчиваться точкой"
            input.contains("..") -> "Email не может содержать две точки подряд"
            !input.contains(".") -> "Неверный формат email — нет точки"
            input.substringAfter("@").isEmpty() -> "Неверный формат email"
            else -> null
        }
    }

    fun validatePassword(input: String): String? {
        return when {
            input.isEmpty() -> "Введи пароль"
            input.contains(" ") -> "Пароль не должен содержать пробелы"
            input.length < 6 -> "Пароль минимум 6 символов"
            else -> null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📖", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MoodArchive",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isLoginMode) "Войди чтобы продолжить" else "Создай новый аккаунт",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TextField(
                    value = email,
                    onValueChange = { input ->
                        val filtered = input
                            .replace(" ", "")
                            .let { if (it.startsWith(".")) it.drop(1) else it }
                        email = filtered
                        errorMessage = ""
                    },
                    placeholder = {
                        Text(
                            "Email",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                TextField(
                    value = password,
                    onValueChange = { input ->
                        password = input.replace(" ", "")
                        errorMessage = ""
                    },
                    placeholder = {
                        Text(
                            "Пароль",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val emailError = validateEmail(email)
                    if (emailError != null) {
                        errorMessage = emailError
                        return@Button
                    }

                    val passwordError = validatePassword(password)
                    if (passwordError != null) {
                        errorMessage = passwordError
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            if (isLoginMode) {
                                val result = auth.signInWithEmailAndPassword(
                                    email.trim(),
                                    password
                                ).await()
                                val userId = result.user?.uid ?: ""
                                onAuthSuccess(userId)
                            } else {
                                val result = auth.createUserWithEmailAndPassword(
                                    email.trim(),
                                    password
                                ).await()
                                val userId = result.user?.uid ?: ""
                                onAuthSuccess(userId)
                            }
                        } catch (e: Exception) {
                            errorMessage = when {
                                e.message?.contains("email address is already in use") == true ->
                                    "Этот email уже используется"
                                e.message?.contains("no user record") == true ->
                                    "Пользователь не найден"
                                e.message?.contains("password is invalid") == true ->
                                    "Неверный пароль"
                                e.message?.contains("badly formatted") == true ->
                                    "Неверный формат email"
                                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                                    "Неверный email или пароль"
                                else -> "Ошибка входа"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (isLoginMode) "Войти" else "Зарегистрироваться",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = {
                isLoginMode = !isLoginMode
                errorMessage = ""
                email = ""
                password = ""
            }) {
                Text(
                    text = if (isLoginMode)
                        "Нет аккаунта? Зарегистрироваться"
                    else
                        "Уже есть аккаунт? Войти",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
            }
        }
    }
}