cat app/src/main/java/com/example/presentation/ui/MainApp.kt | \
sed -e 's/composable("library") {/composable("local_audio") {\n                        val repository = com.example.di.DependencyProvider.repository!!\n                        com.example.features.offlinecache.LocalAudioScreen(repository = repository)\n                    }\n                    composable("library") {/g' \
> new_MainApp.kt
mv new_MainApp.kt app/src/main/java/com/example/presentation/ui/MainApp.kt
