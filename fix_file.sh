cat app/src/main/java/com/example/presentation/player/PlayerStateHolder.kt | awk '
BEGIN { skip = 0 }
/                        }/ {
    if (NR == 404 || NR == 405 || NR == 406 || NR == 407 || NR == 408) { next }
}
/                } catch \(e: Exception\) {/ {
    if (NR == 406) { next }
}
/                    Log.e\(TAG, "Piped API Error: \$\{e.message\}"\)/ {
    if (NR == 407) { next }
}
/                }/ {
    if (NR == 408) { next }
}
{ print }
' > fixed.kt
mv fixed.kt app/src/main/java/com/example/presentation/player/PlayerStateHolder.kt
