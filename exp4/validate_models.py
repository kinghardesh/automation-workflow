from pathlib import Path
import xml.etree.ElementTree as ET
BASE=Path(__file__).parent
B='http://www.omg.org/spec/BPMN/20100524/MODEL'
Z='http://camunda.org/schema/zeebe/1.0'
D='https://www.omg.org/spec/DMN/20191111/MODEL/'
expected={
 'lab1-loan-approval.bpmn':('loanDecision',{'validate-applicant','io.camunda:http-json:1'}),
 'lab2-order-processing.bpmn':('orderPriority',{'validate-order-check-stock','io.camunda:http-json:1','fulfil-order'}),
 'lab3-hospital-triage.bpmn':('triageCategory',{'validate-registration','io.camunda:http-json:1','book-appointment'})}
errors=[]
for fn,(decision,jobtypes) in expected.items():
 root=ET.parse(BASE/'bpmn'/fn).getroot()
 ids={e.attrib['id'] for e in root.iter() if e.tag in (f'{{{B}}}startEvent',f'{{{B}}}endEvent',f'{{{B}}}serviceTask',f'{{{B}}}businessRuleTask',f'{{{B}}}userTask',f'{{{B}}}exclusiveGateway',f'{{{B}}}boundaryEvent',f'{{{B}}}sequenceFlow') and 'id' in e.attrib}
 for flow in root.findall('.//{%s}sequenceFlow'%B):
  if flow.attrib.get('sourceRef') not in ids: errors.append(f'{fn}: missing source {flow.attrib.get("sourceRef")}')
  if flow.attrib.get('targetRef') not in ids: errors.append(f'{fn}: missing target {flow.attrib.get("targetRef")}')
 tasks={x.attrib.get('type') for x in root.findall('.//{%s}taskDefinition'%Z)}
 if not jobtypes.issubset(tasks): errors.append(f'{fn}: missing task types {jobtypes-tasks}')
 if not root.find(f'.//{{{B}}}businessRuleTask[@id="Dmn"]/{{{B}}}extensionElements/{{{Z}}}calledDecision[@decisionId="{decision}"]') is not None:
  errors.append(f'{fn}: decision reference mismatch for {decision}')
 print(f'OK: {fn}: {len(root.findall(".//{%s}sequenceFlow"%B))} flows, {len(root.findall(".//{%s}userTask"%B))} user tasks')
for f in (BASE/'dmn').glob('*.dmn'):
 r=ET.parse(f).getroot(); dec=r.find('{%s}decision'%D)
 if dec is None: errors.append(f'{f.name}: no DMN decision')
 else: print(f'OK: {f.name}: decision id {dec.attrib.get("id")}')
for f in (BASE/'forms').glob('*.form'):
 import json
 try: json.loads(f.read_text())
 except Exception as e: errors.append(f'{f.name}: invalid JSON: {e}')
if errors:
 print('\nERRORS:'); print('\n'.join(errors)); raise SystemExit(1)
print('\nStatic XML/JSON/reference checks passed. This does not replace Camunda deployment or runtime testing.')
