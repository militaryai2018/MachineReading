from common import get_ngram, get_match_size, get_trim_string


def test_ngram_1():
    sentence = '我是孙维松'
    n_size = 2
    print(get_ngram(sentence, n_size))


def test_ngram_2():
    sentence = '中华人民共和国'
    n_size = 1
    print(get_ngram(sentence, n_size))


def test_match_size_1():
    cand = '我是中华人民共和国公民'
    ref = '中华人民的一份子我'
    n_size = 2
    cand_ngram = get_ngram(cand, n_size)
    ref_ngram = get_ngram(ref, n_size)
    match_size, cand_size = get_match_size(cand_ngram, ref_ngram)
    print('match size: {}'.format(match_size))
    print('cand size: {}'.format(cand_size))


def test_match_size_2():
    cand = '我我我我我我'
    ref = '中华人民的一份子我'
    n_size = 1
    cand_ngram = get_ngram(cand, n_size)
    ref_ngram = get_ngram(ref, n_size)
    match_size, cand_size = get_match_size(cand_ngram, ref_ngram)
    print('match size: {}'.format(match_size))
    print('cand size: {}'.format(cand_size))


def test_trim_string():
    sentence = '我的， 达芬奇激发的  扩大。'
    sentence = get_trim_string(sentence)
    print(sentence)


if __name__ == '__main__':
    test_ngram_1()
    test_ngram_2()
    test_match_size_1()
    test_match_size_2()
    test_trim_string()
