from rouge import RougeL
from bleu import Bleu


def calcu_score(ref_list: list, cand_list: list) -> tuple:
    """计算bleu和rouge得分

    Arguments:
        ref_list {list} -- 参考答案列表
        cand_list {list} -- 生成答案列表

    Returns:
        tuple -- 计算出的分数元祖(bleu_score, rouge_score)
    """

    eval_bleu = Bleu()
    eval_rouge = RougeL()

    for idx, reference in enumerate(ref_list):
        candidate = cand_list[idx]
        if reference is None or reference == '':
            pass
        eval_bleu.add_inst(candidate, reference)
        eval_rouge.add_inst(candidate, reference)

    return eval_bleu.get_score(), eval_rouge.get_score()


if __name__ == '__main__':
    ref_list = ['adddd']
    cand_list = ['fafeqr']
    print(calcu_score(ref_list, cand_list))
