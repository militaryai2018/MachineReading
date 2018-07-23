from bleu import Bleu


N_SIZE = 4


def test_count_bp():
    cand = '我是中国人'
    ref = '重视啊啊啊啊我啊啊我了'
    bleu = Bleu(N_SIZE)
    bp = bleu.count_bp(cand, ref)
    print('BP: {}'.format(bp))


def test_count_bp2():
    cand = '我是中国人当外人大气'
    ref = '重视啊啊'
    bleu = Bleu(N_SIZE)
    bp = bleu.count_bp(cand, ref)
    print('BP: {}'.format(bp))


def test_add_inst():
    cand = '13'
    ref = '13'
    bleu = Bleu(N_SIZE)
    bleu.add_inst(cand, ref)
    match_ngram = bleu.match_ngram
    candi_ngram = bleu.candi_ngram
    print('match_ngram: {}'.format(match_ngram))
    print('candi_ngram: {}'.format(candi_ngram))


def test_score():
    cand = "中华人民共和国"
    ref = "中华人民共和国公民"
    bleu = Bleu(N_SIZE)
    s = bleu.score(cand, ref)
    print('score: {}'.format(s))


if __name__ == '__main__':
    # test_count_bp()
    # test_count_bp2()
    # test_ngram()
    # test_score()
    test_add_inst()
