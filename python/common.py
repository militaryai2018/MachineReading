from collections import defaultdict
import re


def get_match_size(cand_ngram: list, ref_ngram: list) -> (int, int):
    ref_set = defaultdict(int)
    cand_set = defaultdict(int)

    for ngram in ref_ngram:
        ref_set[ngram] += 1

    for ngram in cand_ngram:
        cand_set[ngram] += 1
    match_size = 0
    for ngram in cand_set:
        match_size += min(cand_set[ngram], ref_set[ngram])
    cand_size = len(cand_ngram)
    return match_size, cand_size


def get_ngram(sent: str, n_size: int) -> list:
    ngram_list = [sent[left: left + n_size]
                  for left in range(len(sent) - n_size + 1)]
    return ngram_list


def get_trim_string(string: str) -> str:
    """
    """
    string = re.sub(
        r'\s+', '', string)

    return string


def word2char(str_in):
    str_out = str_in.replace(' ', '')
    return ''.join(str_out.split())


if __name__ == '__main__':
    cand = '我是中国人'
    ref = '我是孙维松'
    cand_ngram = get_ngram(cand, 0)
    ref_ngram = get_ngram(ref, 0)
    print('cand_ngram: {}'.format(cand_ngram))
    print('ref_ngram: {}'.format(ref_ngram))
    # match_size, cand_size = get_match_size(cand_ngram, ref_ngram)
