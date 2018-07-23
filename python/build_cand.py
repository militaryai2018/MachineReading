import json


def build_cand_file():
    with open('./data/all.json', mode='r', encoding='utf-8') as fp:
        all_json: dict = json.load(fp)
    cand = []
    
    for article in all_json:
        article_id = article['article_id']
        questions = []
        for question_obj in article['questions']:
            question_id = question_obj['questions_id']
            answer = question_obj['answer']
            cand_quesion = {'questions_id': question_id, 'answer': answer}
            questions.append(cand_quesion)
        cand.append({'article_id': article_id, "questions": questions})
    
    with open('./data/cand.json', mode='w', encoding='utf-8') as fp:
        json.dump(cand, fp, ensure_ascii=False)


if __name__ == '__main__':
    build_cand_file()