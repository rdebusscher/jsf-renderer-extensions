/*
 * Copyright 2014-2020 Rudy De Busscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.ee.jsf.valerie.recording;

import be.atbash.ee.jsf.jerry.metadata.PropertyInformationKeys;
import be.atbash.ee.jsf.valerie.utils.MethodHandleUtils;
import be.atbash.util.exception.AtbashUnexpectedException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 *
 */
@ApplicationScoped
public class RecordingInfoManager {

    @Inject
    private Validator validator;

    public void keepInfo(FacesContext facesContext, RecordValueInfo recordValueInfo, Object data) {

        List<RecordValueData> recordedData = (List<RecordValueData>) facesContext.getAttributes().get(PropertyInformationKeys.RECORDING_INFORMATION);

        if (recordedData == null) {
            recordedData = new ArrayList<>();
            facesContext.getAttributes().put(PropertyInformationKeys.RECORDING_INFORMATION, recordedData);

        }
        recordedData.add(new RecordValueData(recordValueInfo, data));
    }

    public boolean processClassLevelConstraints(FacesContext facesContext) {
        List<RecordValueData> recordedData = (List<RecordValueData>) facesContext.getAttributes().get(PropertyInformationKeys.RECORDING_INFORMATION);

        if (recordedData == null) {
            return true;
        }
        boolean result = true;

        Map<RecordValueInfo.Key, List<RecordValueData>> grouped = new HashMap<>();

        for (RecordValueData recordValueData : recordedData) {
            RecordValueInfo.Key key = recordValueData.getRecordValueInfo().getKey();
            List<RecordValueData> recordValueDatas = grouped.computeIfAbsent(key, k -> new ArrayList<>());
            recordValueDatas.add(recordValueData);
        }

        for (Map.Entry<RecordValueInfo.Key, List<RecordValueData>> entry : grouped.entrySet()) {
            RecordValueInfo.Key key = entry.getKey();
            Object value = createValueObject(key);
            for (RecordValueData recordValueData : entry.getValue()) {
                fillInData(value, recordValueData.getRecordValueInfo().getClassProperty(), recordValueData.getData());
            }

            Set<ConstraintViolation<Object>> violations = validator.validate(value, Default.class);

            if (processViolations(violations, key.getValidator())) {
                result = false;
            }

        }
        return result;
    }

    private boolean processViolations(Set<ConstraintViolation<Object>> violations, Class<? extends ConstraintValidator> validator) {
        boolean result = false;
        for (ConstraintViolation<Object> violation : violations) {
            List<?> validatorClasses = violation.getConstraintDescriptor().getConstraintValidatorClasses();
            if (validatorClasses.contains(validator)) {
                showMessage(violation.getMessage());
                result = true;
            }
        }
        return result;
    }

    private void showMessage(String message) {
        FacesContext context = FacesContext.getCurrentInstance();

        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }

    private void fillInData(Object target, String classProperty, Object data) {
        if (data == null) {
            // Nothing to do
            return;
        }
        MethodHandle handle = MethodHandleUtils.getSetterHandle(target.getClass(), classProperty, data.getClass());
        if (handle == null) {
            return; // getSetterHandle already logs warning
        }
        try {
            handle.invokeWithArguments(target, data);
        } catch (Throwable throwable) {
            throw new AtbashUnexpectedException(throwable);
        }
    }

    private Object createValueObject(RecordValueInfo.Key key) {
        Object result;
        Constructor<?> constructor;
        try {
            constructor = key.getTargetClass().getConstructor(new Class[]{}); // TODO Verify if we can remove the class Array parameter
            result = constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new AtbashUnexpectedException(e);
        }
        return result;
    }

}
