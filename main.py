import os
import re
import glob
import unicodedata
from collections import Counter
from typing import List
import pandas as pd
import numpy as np


# ---------------------------------------------------------------------
# Global configuration
# ---------------------------------------------------------------------

TRANSCRIPTIONS_DIR = "transcriptions"
CSV_OUTPUT_PATH = "words_frequency.csv"
NORMALIZATION_EXCEPTIONS = {"campus", "qualitas", "corpus"}


# ---------------------------------------------------------------------
# Utility functions
# ---------------------------------------------------------------------

def load_transcriptions(directory: str) -> str:
    """Read and concatenate all transcription files into a single lowercase string."""
    os.makedirs(directory, exist_ok=True)
    files = sorted(glob.glob(os.path.join(directory, "*.txt")))
    full_text = ""

    for file_path in files:
        with open(file_path, "r", encoding="utf-8") as f:
            full_text += f.read().lower() + " "

    return full_text


def extract_words(text: str) -> List[str]:
    """Extract Portuguese words (with accents) using regex."""
    return re.findall(r"\b[a-záéíóúâêôãõç]+\b", text)


def remove_accents(text: str) -> str:
    """Remove accents from a string."""
    return "".join(
        c for c in unicodedata.normalize("NFD", text)
        if unicodedata.category(c) != "Mn"
    )


def is_verb_heuristic(token: str, verb_endings: tuple[str, ...]) -> bool:
    """Heuristic rule to check if a token is likely a verb based on suffix patterns."""
    return any(token.endswith(suf) for suf in verb_endings)


def normalize_plural(word: str) -> str:
    """Normalize plural forms into singular (heuristic approach)."""
    word = word.lower()

    if word in NORMALIZATION_EXCEPTIONS:
        return word
    if word == "funções":
        return "função"
    if word.endswith("eis"):
        return word[:-3] + "el"
    if word.endswith(("oes", "aes")):
        return word[:-3] + "ao"
    if word.endswith(("is", "es")):
        return word[:-1]
    if word.endswith("s"):
        return word[:-1]

    return word


# ---------------------------------------------------------------------
# Word filtering lists
# ---------------------------------------------------------------------

STOPWORDS = {
    "que", "de", "do", "da", "em", "para", "com", "os", "as", "um", "uma", "no", "na",
    "se", "ele", "ela", "aqui", "isso", "pra", "tu", "você", "né", "tá", "então", "mas",
    "bem", "ali", "aí", "já", "só", "certo", "cara", "bom", "tipo", "assim", "sim", "não",
    "ok", "tudo", "agora", "também", "ainda", "muito", "pouco", "talvez", "porque", "por",
    "onde", "quando", "quanto", "como", "sobre", "entre", "todos", "todas", "cada", "mesmo",
    "mesma", "esse", "essa", "esses", "essas", "aquele", "aquela", "aqueles", "aquelas",
    "aqui", "ali", "lá", "eu", "mais", "gente", "te", "coisa", "coisas", "alguma",
    "algumas", "qual", "acha", "entendeu", "beleza", "pronto", "geralmente", "ah", "vezes",
    "até", "outro", "muita", "eles", "deixa", "algum", "tempo", "outras", "tua",
    "basicamente", "forma", "dos", "alguns", "dá", "uhum", "me", "minha", "mim", "parece",
    "teu", "normalmente", "das", "principalmente", "depois", "ao", "estão", "peguntas",
    "realmente", "uso", "feito", "hoje", "dele", "quais", "vez", "fica", "consigo",
    "acredito", "fosse", "trabalha", "tanto", "pelo", "desses", "sem", "gosto", "segundo",
    "end", "deu", "conta", "vários", "fazem", "seja", "hora", "viu", "tão", "dessa", "vê",
    "meio", "utilizo", "nessa", "duas", "vejo", "nem", "vem", "quem", "for", "tô", "além",
    "anos", "três", "nome", "vão", "link", "olhada", "entendi", "back", "tranquilo",
    "provavelmente", "adriano", "tal", "nos", "há", "outros", "pro", "si", "consegue",
    "momento", "meu", "faço", "sensação", "pessoa", "dois", "parte", "outra", "dentro",
    "sempre", "menos", "dados", "linha", "linhas", "todo", "algo", "ponto", "pontos",
    "pessoas", "nunca", "disso", "ti", "acaba", "boa", "vida", "mundo", "depende",
    "pela", "inclusive"
}

ARTICLES = {"o", "a", "os", "as", "um", "uma", "uns", "umas"}

EXTRA_REMOVAL = {"é", "e"}

COMMON_VERBS = {
    "está", "vai", "acho", "foi", "era", "são", "ser", "tá", "tem", "tinha", "teve", "fui",
    "sou", "vamos", "estava", "foram", "iria", "seria", "posso", "pode", "podia", "deve",
    "deveria", "estar", "fazer", "fiz", "fez", "faz", "fazendo", "tenho", "ter", "haver",
    "havia", "quero", "quer", "queria", "ver", "dizer", "disse", "diz", "saber", "sabe",
    "usar", "usa", "usando", "utilizar", "utiliza", "precisa", "precisar", "preciso"
}

VERB_ENDINGS = (
    "ar", "er", "ir", "ando", "endo", "indo",
    "ou", "ei", "amos", "emos", "imos",
    "ava", "ia", "aram", "eram", "iram",
    "arei", "eria", "iria", "asse", "esse", "isse",
    "armos", "ermos", "irmos", "ado", "ido"
)


# ---------------------------------------------------------------------
# Main processing pipeline
# ---------------------------------------------------------------------

def filter_words(words: List[str]) -> List[str]:
    """Remove stopwords, articles, verbs, and unwanted tokens."""
    return [
        w for w in words
        if w not in STOPWORDS
        and w not in ARTICLES
        and w not in EXTRA_REMOVAL
        and w not in COMMON_VERBS
        and not is_verb_heuristic(w, VERB_ENDINGS)
    ]


def compute_frequencies(words: List[str], top_n: int = 90) -> pd.DataFrame:
    """Compute word frequency and apply logarithmic scaling."""
    counter = Counter(words)
    df = pd.DataFrame(counter.most_common(top_n), columns=["word", "frequency"])
    df["frequency_log"] = np.log1p(df["frequency"])
    return df


def main() -> None:
    """Run the complete text-processing pipeline."""
    text = load_transcriptions(TRANSCRIPTIONS_DIR)
    words = extract_words(text)
    filtered = filter_words(words)
    normalized = [normalize_plural(w) for w in filtered]

    df_freq = compute_frequencies(normalized)
    df_freq.to_csv(CSV_OUTPUT_PATH, index=False, encoding="utf-8")

    print(f"Word frequencies saved to: {CSV_OUTPUT_PATH}")


if __name__ == "__main__":
    main()
