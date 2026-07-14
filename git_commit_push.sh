#!/bin/bash
cd /Users/aarondavid/Documents/com.agape.sovereign.ai || exit 1
{
  echo "=== $(date) ==="
  cat >> .gitignore << 'EOF'
/jdk17
build_release.sh
build_log.txt
build_done.flag
EOF
  git add -A
  echo "--- staged status ---"
  git status
  git commit -m "Update Architect AI MCP integration and build release APK/AAB for com.agape.sovereign.ai"
  echo "COMMIT_EXIT=$?"
  git push origin main
  echo "PUSH_EXIT=$?"
  echo "=== done ==="
} > git_log.txt 2>&1
