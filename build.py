#!/usr/bin/env python3
"""Build the self-contained HTML tools with base64-embedded local models.

Usage:
    python3 build.py                 # uses ./models, writes ./docs
    python3 build.py --repo-url URL  # URL used in the "view source" footers

Models are downloaded separately (see README.md) into ./models.
Nothing here talks to the network.
"""
from __future__ import annotations

import argparse
import base64
import json
import pathlib
import shutil
import sys

ROOT = pathlib.Path(__file__).resolve().parent
SRC = ROOT / "src"
MODELS = ROOT / "models"
OUT = ROOT / "docs"

# Order matters: it defines byte offsets in the concatenated blob.
WHISPER_FILES = [
    "config.json",
    "generation_config.json",
    "preprocessor_config.json",
    "tokenizer.json",
    "tokenizer_config.json",
    "added_tokens.json",
    "special_tokens_map.json",
    "encoder_model_quantized.onnx",
    "decoder_model_merged_quantized.onnx",
]
CHAT_FILES = ["model_q4f16.onnx"]
STATIC_PAGES = ["cassandra-course.html", "copilot-pm-course.html", "claude-code-course.html", "kafka-course.html"]  # plain pages without models, copied verbatim
CHAT_PART_CHARS = 52_428_800  # ~50 MiB of base64 per part (multiple of 4)


def read_tree(directory: pathlib.Path, names: list[str]) -> tuple[bytes, list[dict]]:
    blob = bytearray()
    manifest = []
    for name in names:
        path = directory / name
        if not path.exists():
            sys.exit(f"missing model file: {path}")
        data = path.read_bytes()
        manifest.append({"name": name, "size": len(data)})
        blob += data
    return bytes(blob), manifest


def b64(data: bytes) -> str:
    return base64.b64encode(data).decode("ascii")


def build_stt(repo_url: str, stamp: str) -> pathlib.Path:
    blob, manifest = read_tree(MODELS / "whisper", WHISPER_FILES)
    payload = b64(blob)
    html = (SRC / "speech-to-text.template.html").read_text(encoding="utf-8")
    html = html.replace("__MANIFEST_JSON__", json.dumps({"files": manifest}, separators=(",", ":")))
    html = html.replace("__MODEL_B64__", payload)
    html = html.replace("__STT_B64_MB__", f"{len(payload) / 1e6:.1f}")
    html = html.replace("__REPO_URL__", repo_url)
    html = html.replace("__BUILD_STAMP__", stamp)
    out = OUT / "speech-to-text.html"
    out.write_text(html, encoding="utf-8")
    return out


def build_chat(repo_url: str, stamp: str) -> pathlib.Path:
    blob, manifest = read_tree(MODELS / "chat", CHAT_FILES)
    payload = b64(blob)
    parts = []
    for i in range(0, len(payload), CHAT_PART_CHARS):
        chunk = payload[i : i + CHAT_PART_CHARS]
        assert len(chunk) % 4 == 0, "base64 part must be a multiple of 4 chars"
        name = f"chat-model.b64.part{len(parts) + 1}.txt"
        (OUT / name).write_text(chunk, encoding="ascii")
        parts.append(name)
    html = (SRC / "chat.template.html").read_text(encoding="utf-8")
    model_manifest = {"total": len(blob), "files": manifest, "parts": parts}
    html = html.replace("__MANIFEST_JSON__", json.dumps(model_manifest, separators=(",", ":")))
    html = html.replace("__REPO_URL__", repo_url)
    html = html.replace("__BUILD_STAMP__", stamp)
    out = OUT / "chat.html"
    out.write_text(html, encoding="utf-8")
    return out


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

    stt = build_stt(args.repo_url, stamp)
    chat = build_chat(args.repo_url, stamp)
    for name in STATIC_PAGES:
        shutil.copyfile(SRC / name, OUT / name)

    total = sum(f.stat().st_size for f in OUT.iterdir() if f.is_file())
    index = (SRC / "index.template.html").read_text(encoding="utf-8")
    index = index.replace("__STT_HTML_MB__", f"{stt.stat().st_size / 1e6:.0f}")
    index = index.replace("__CHAT_MODEL_MB__", "117")
    index = index.replace("__REPO_URL__", args.repo_url)
    index = index.replace("__BUILD_STAMP__", stamp)
    (OUT / "index.html").write_text(index, encoding="utf-8")

    for f in sorted(OUT.iterdir()):
        if f.is_file():
            print(f"{f.stat().st_size / 1e6:9.1f} MB  {f.name}")
    print(f"{total / 1e6:9.1f} MB  TOTAL ({len(list(OUT.iterdir()))} files)")


if __name__ == "__main__":
    main()