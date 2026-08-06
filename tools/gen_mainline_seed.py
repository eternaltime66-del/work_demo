#!/usr/bin/env python3
"""Shim: 本机用 Node 生成种子。等价命令: node tools/gen_mainline_seed.js"""
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parent
js = root / "gen_mainline_seed.js"
raise SystemExit(subprocess.call(["node", str(js)], cwd=str(root.parent)))
