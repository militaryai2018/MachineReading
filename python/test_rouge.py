from rouge import RougeL

rouge = RougeL()


def test_lcs():
    cand = '中华人民共和国中的人'
    ref = '中国人民共和国的'
    lcs = rouge.lcs(cand, ref)
    print('lcs: {}'.format(lcs))


def test_lcs2():
    cand = '中华人民'
    ref = '中国人民共和国的'
    lcs = rouge.lcs(cand, ref)
    print('lcs: {}'.format(lcs))


def test_score():
    cand = '中华人民共和国中华人民共和国中华人民共和国中华人民共和国中华人民共和国中华人民共和国'
    ref = '中华人民共和国的'
    rouge.add_inst(cand, ref)
    score = rouge.get_score()
    print('score: {}'.format(score))


if __name__ == '__main__':
    test_lcs()
    test_lcs2()
    test_score()
