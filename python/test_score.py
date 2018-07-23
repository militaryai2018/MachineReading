import json

from bleu import Bleu
from rouge import RougeL


def read_json() -> list:
    """读取json文件

    Returns:
        list -- N * 2列的列表，每列分别保存参考答案和预测答案
    """
    with open('./data/test_data.json', mode='r', encoding='utf-8') as f:
        data = json.load(f)
    return data


def test_score():
    # init all argument
    data = read_json()

    rouge_eval = RougeL()
    bleu_eval = Bleu()
    for idx, (ref_key, cand_key) in enumerate(data):
        ref_sent = data[idx][ref_key]
        cand_sent = data[idx][cand_key]

        rouge_eval.add_inst(cand_sent, ref_sent)
        bleu_eval.add_inst(cand_sent, ref_sent)

    bleu_score = bleu_eval.get_score()
    rouge_score = rouge_eval.get_score()
    print('bleu score: {}, rouge score: {}'.format(bleu_score, rouge_score))
# def test_scores():
#     # initail argument
#     data = read_json()
#     score_list = []

#     # count scores
#     for idx, (ref_key, cand_key) in enumerate(data):
#         ref_sent = data[idx][ref_key]
#         cand_sent = data[idx][cand_key]

#         score = {'rougeL': rougel.score(cand_sent, ref_sent),
#                  "bleu": bleu.score(cand_sent, ref_sent)}
#         score_list.append(score)
#     # save data
#     with open('./data/test_score.json', mode='w', encoding='utf-8') as f:
#         json.dump(score_list, f)


# def test_score7():
#     ref_sent = '正常情况下，Cys C在血清和血浆中的浓度为≤1.03mg/L (参考范围).'
#     cand_sent = '正常情况下，Cys C在血清和血浆中的浓度为0.51-1.09mg/L (参考范围)。当肾功能受损时，Cys C在血液中的浓度随肾小球滤过率变化而变化.肾衰时, 肾小球滤过率下降，Cys C在血液中浓度可增加10多倍;若肾小球滤过率正常,而肾小管功能失常时，会阻碍Cys C在肾小管吸收并迅速分解,使尿中的浓度增加100多倍。'
#     rougel = RougeL()
#     bleu = Bleu()
#     print('bleu score: {}'.format(bleu.score(cand_sent, ref_sent)))
#     print('rouge score: {}'.format(rougel.score(cand_sent, ref_sent)))


# def test_score5():
#     ref_sent = 'wlcm。'
#     cand_sent = 'yw。'
#     rougel = RougeL()
#     bleu = Bleu()
#     print('bleu score: {}'.format(bleu.score(cand_sent, ref_sent)))
#     print('rouge score: {}'.format(rougel.score(cand_sent, ref_sent)))


# def test_score8():
#     ref_sent = '登入淘宝网。点击账号名。点击账号名之后，点击”我的评价“。”买家累计信用“就是你的淘宝账号等级。'
#     cand_sent = '登录之后，右上角我的淘宝，红色标记处，然后点击打开。就会看到你自己的信用等级。'
#     rougel = RougeL()
#     bleu = Bleu()
#     print('bleu score: {}'.format(bleu.score(cand_sent, ref_sent)))
#     print('rouge score: {}'.format(rougel.score(cand_sent, ref_sent)))


if __name__ == '__main__':
    # test_scores()
    # test_score8()
    # test_score5()
    test_score()
