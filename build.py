#!/usr/bin/env python3
"""Build the self-contained HTML tools (animated knowledge pages).

Usage:
    python3 build.py                 # writes ./docs
    python3 build.py --repo-url URL  # URL used in the "view source" footers
"""
from __future__ import annotations

import argparse
import pathlib
import shutil

ROOT = pathlib.Path(__file__).resolve().parent
SRC = ROOT / "src"
OUT = ROOT / "docs"

STATIC_PAGES = ["cassandra-course.html", "copilot-pm-course.html", "claude-code-course.html", "claude-code-course-pl.html", "kafka-course.html", "system-design-course.html", "spark-course.html", "db-app-course.html", "k8s-course.html"]  # plain pages, copied verbatim


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--repo-url", default="https://github.com/gbryk1/html-tools-local-llm")
    ap.add_argument("--clean", action="store_true", help="delete docs/ first")
    args = ap.parse_args()

    if args.clean and OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True, exist_ok=True)

    import datetime

    stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%d %H:%M UTC")

    for name in STATIC_PAGES:
        shutil.copyfile(SRC / name, OUT / name)

    index = (SRC / "index.template.html").read_text(encoding="utf-8")
    index = index.replace("__REPO_URL__", args.repo_url)
    index = index.replace("__BUILD_STAMP__", stamp)
    (OUT / "index.html").write_text(index, encoding="utf-8")

    total = sum(f.stat().st_size for f in OUT.iterdir() if f.is_file())
    for f in sorted(OUT.iterdir()):
        if f.is_file():
            print(f"{f.stat().st_size / 1e6:9.1f} MB  {f.name}")
    print(f"{total / 1e6:9.1f} MB  TOTAL ({len(list(OUT.iterdir()))} files)")


if __name__ == "__main__":
    main()
